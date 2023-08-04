package com.tcwang.interfaces.core;

import com.tcwang.interfaces.TGet;

import java.lang.invoke.SerializedLambda;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author: JKin
 * @createTime: 2023/7/31 10:19
 * @fileDescriptions: You had better view this comment before you use it.
 */
public class LambdaToFiledNameUtils {
    /**
     * 通过这个方法拿到getter这个方法名字, 然后变成对应的属性名字
     * @param getter
     * @param <T>
     *     如果一个序列化类中含有Object writeReplace()方法，那么实际序列化的对象将是作为writeReplace方法
     *     返回值的对象，而且序列化过程的依据是实际被序列化对象的序列化实现。
     */
    public static<T> String getFiledName(TGet<T> getter)  {
        Method writeReplace = null;
        try {
            writeReplace = getter.getClass().getDeclaredMethod("writeReplace");
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
        //设置这个方法可以被访问
        writeReplace.setAccessible(true);
        //调用这个方法拿到对应的序列化对象
        SerializedLambda lambda = null;
        try {
            lambda = (SerializedLambda)writeReplace.invoke(getter);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }
        //拿到这个被序列化的方法之后, 我们就可以得到相关的方法名称了
        final String getterName = lambda.getImplMethodName();
        //输出这个方法的名字
        //如果要验证的话,可以经过判断这个方法是不是以get开头的, 这样的话可可以实现代码的安全
        return transferGetterNameToFieldName(getterName);
    }

    public static String transferGetterNameToFieldName(String getterName){
        StringBuilder filedNameStringBuilder = new StringBuilder();
        if (!getterName.startsWith("get")){
           throw  new RuntimeException("Method is not a get method ");
        }
        char[] chars = getterName.toCharArray();
        char firstUppercaseChar = chars[3];
        filedNameStringBuilder.append(transACharToLowerCase(firstUppercaseChar));
        for (int i = 4 ; i < chars.length; i++){
            filedNameStringBuilder.append(chars[i]);
        }
        return filedNameStringBuilder.toString();
    }

    /**
     * 转换一个 大写的char 到 小写的char
     * @param upperCaseChar
     * @return
     */
    public static char transACharToLowerCase(char upperCaseChar){
        //如果char > 127 直接返回
        if ( upperCaseChar > 127){
            return upperCaseChar;
        }else{
            //那么再判断是不是落在了 大写字母的范围内
            if ( upperCaseChar >= 65 && upperCaseChar <=90){
                //97 - 65
                return (char)(upperCaseChar + 32);
            }
        }
        return  upperCaseChar;
    }

    public static boolean isUpperCaseAlpha(char alpha) {
        //如果char > 127 直接返回
        if (alpha > 127) {
            return false;
        } else {
            //那么再判断是不是落在了 大写字母的范围内
            if (alpha >= 65 && alpha <= 90) {
                //97 - 65
                return true;
            }
        }
        return false;
    }

}