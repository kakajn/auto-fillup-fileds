package com.tcwang.interfaces.core;


import com.tcwang.anno.MappingFiledName;
import com.tcwang.interfaces.TGet;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @author: kakajn
 * @createTime: 2023/7/17 11:58
 * @fileDescriptions:
 */

@Slf4j
public class FillUpMappingUtils {

    /**
     *
     */
    private static final Map<String,List<Map<String, Field>>> cache = new ConcurrentHashMap();

    /**
     *
     * @param t 填充的数据源对象
     * @param r 被填充的对象
     * @return
     * @param <T> 填充的数据源的类型
     * @param <R> 返回值类型, 以及第二个参数类型
     */
    public static<T,R> R fillUpByObject(T t , R r){
        return fillUpByObjectExclude(t,r,null);
    }

    public static<T,R> R fillUpByObjectExclude(T t , R r, TGet<T>... excludes){
        if (t == null || r == null){
            log.info("Both param t and r must be not null , but now t is {} and r is {}", t , r);
            throw new RuntimeException("参数t和r必须都必须为非null值");
        }

        Map<String, Field> dataSourceStringFieldMap = null;

        Map<String, Field> filledStringFieldMap = null;

        //查询缓存看里面存不存在
        String cacheKey = generateCacheKey(t,r);
        if (hasClassCache(cacheKey)){
            dataSourceStringFieldMap = tryGetDataSourceMetaDataFromClassCache(cacheKey);
            filledStringFieldMap = tryGetFieldMetaDataFromClassCache(cacheKey);
        }else{
            //T 的Class类型
            Class<?> tClass = t.getClass();

            //R 的Class类型
            Class<?> rClass = r.getClass();

            //填充数据的数据源
            Field[] dataSourceDeclaredFields = tClass.getDeclaredFields();
            dataSourceStringFieldMap = tryGetAllFieldNameAndTypeByReflection(dataSourceDeclaredFields);

            //被填充的对象
            Field[] filledDeclaredFields = r.getClass().getDeclaredFields();
            filledStringFieldMap = tryGetAllFieldNameAndTypeByReflection(filledDeclaredFields);

            //创建List
            List<Map<String,Field>> cacheList = new ArrayList<>();
            cacheList.add(dataSourceStringFieldMap);
            cacheList.add(filledStringFieldMap);

            //进行缓存加入
            cache.put(cacheKey,cacheList);
        }

        //获取所有的filledDeclaredFields的键
        Set<String> filledFieldNameSet = filledStringFieldMap.keySet();

        //获取所有要进行排除的字段
        Map<String, Object> stringObjectMapExcludes = null ;
        if (excludes != null){
            stringObjectMapExcludes = transGetMethodArrayToFieldNameMap(excludes);
        }

        //尝试进行值的填充
        for (String filedName : filledFieldNameSet ){
            //尝试从数据源中获取相应的Filed对象进行对比
            Field dataSourceFiled = dataSourceStringFieldMap.get(filedName);
            //被填充的数据对象的Filed字段
            Field filledFiled = filledStringFieldMap.get(filedName);
            if (dataSourceStringFieldMap.get(filedName) != null
                    && dataSourceFiled.getType().equals(filledFiled.getType())
                    && (stringObjectMapExcludes == null || !stringObjectMapExcludes.containsKey(filedName))
            ){
                //尝试使用反射进行数据填充
                tryUseReflectionFillData(dataSourceFiled,filledFiled,t,r);
            }
        }
        return r;
    }

    /**
     * JDK 本身就提供了这个方法, 我们进行再次封装即可
     * @param fillMap       提供数据的那个Map
     * @param filledMap     被填充数据的那个Map
     * @return
     * @param <K>
     * @param <V>
     */
    public static<K,V> Map<K,V> fillUpMapByMap(Map<K,V> fillMap, Map<K,V> filledMap){
        filledMap.putAll(fillMap);
        return filledMap;
    }

    public static<T> Map<String,Object> fillUpMapByObject(T fillObject, Map<String, Object> filledMap) {
        if (fillObject == null){
            throw new RuntimeException("填充数据不能为null!");
        }
        //获取这个对象里面的字段素组
        Field[] declaredFields = fillObject.getClass().getDeclaredFields();
        Map<String, Field> stringFieldMap = tryGetAllFieldNameAndTypeByReflection(declaredFields);
        //管不管这个字段是不是 final 和 static 修饰的都进行属性的填充
        Set<String> filledFieldNameSet = stringFieldMap.keySet();

        //尝试进行值的填充
        for (String filedName : filledFieldNameSet ){
            //设置filed的可见性
            Field fillField = stringFieldMap.get(filedName);
            fillField.setAccessible(true);
            Object o = null;
            try {
                o = fillField.get(fillObject);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
            //设置进入这个Map
            filledMap.put(filedName,o);
        }
        return filledMap;
    }

    /**
     * 尝试通过get方法进行获取字段的名字和对应的类型
     * 如果这个类存在getter方法的话可以这样做
     * 获取一个Map, Map的key为字段的名称, Map的value为这个字段的类型
     * @param methods 方法数组
     * @return 见上面
     */
    private static Map<String,? extends Class<?>> tryGetAllFieldNameAndTypeByGetMethod(Method [] methods){
        return  Arrays.stream(methods)
                .filter((method) -> method.getName().startsWith("get"))
                .collect(Collectors.toMap(method -> method.getName().substring(3), Method::getReturnType));
    }

    /**
     * 如果这个类里面没有get方法, 那么只有通过反射进行获取了
     * @param fields 一个类所有的字段数组
     * @return 见上面
     */
    private static Map<String,Field> tryGetAllFieldNameAndTypeByReflection(Field [] fields){
        return Arrays.stream(fields)
                .filter(filed -> {
                    //判断这个filed是不是static 和 final 的, 如果是直接舍弃
                    int modifiers = filed.getModifiers();
                    return !Modifier.isFinal(modifiers) || !Modifier.isStatic(modifiers);})
                .collect(Collectors.toMap(filed ->{
                    //判断上面有没有注解, 如果有注解的话那么就以注解为准
                    MappingFiledName mappingFiledName = filed.getAnnotation(MappingFiledName.class);
                    if (mappingFiledName != null){
                        return mappingFiledName.mappingFiledName();
                    }else{
                        return filed.getName();
                    }
                }, filed -> filed));
    }

    /**
     *
     * @param dataSourceField  源数据字段
     * @param filledFiled   被填充的字段
     * @param t 数据源对象
     * @param r 被填充的对象
     * @param <R>   见上
     * @param <T>   见上
     *
     */
    private static<R,T> void tryUseReflectionFillData(Field dataSourceField, Field filledFiled,T t, R r){
        //设置可见性
        dataSourceField.setAccessible(true);
        filledFiled.setAccessible(true);

        //尝试进行赋值
        try {
            //获取值
            Object o = dataSourceField.get(t);
            //填充值
            filledFiled.set(r,o);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private static boolean hasClassCache(String selectKey){
        return cache.get(selectKey) != null;
    }
    private static Map<String,Field> tryGetDataSourceMetaDataFromClassCache(String selectKey){
        List<Map<String, Field>> mapCache = cache.get(selectKey);
        //如果是源数据的话默认放在第一个位置
        return mapCache.get(0);
    }

    private static Map<String,Field> tryGetFieldMetaDataFromClassCache(String selectKey){
        List<Map<String, Field>> mapCache = cache.get(selectKey);
        //如果是源数据的话默认放在第一个位置
        return mapCache.get(1);
    }

    private static<T,R> String generateCacheKey(T t, R r){
        String prefix = t.getClass().getClassLoader().toString() + "_" + t.getClass().getName();
        String postfix = r.getClass().getClassLoader().toString() + "_" + r.getClass().getName();
        return prefix + "_" + postfix;
    }

    private static<T> Map<String,Object> transGetMethodArrayToFieldNameMap(TGet<T> [] tGets){
        HashMap<String, Object> stringObjectHashMap = new HashMap<>();
        for (int i = 0; i < tGets.length; i++){
            String filedName = LambdaToFiledNameUtils.getFiledName(tGets[i]);
            stringObjectHashMap.put(filedName,null);
        }
        return stringObjectHashMap;
    }

}
