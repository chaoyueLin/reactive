package com.example.rxjava2demo;

/*****************************************************************
 * * File: - BaseEntity
 * * Description: 
 * * Version: 1.0
 * * Date : 2020/9/15
 * * Author: linchaoyue
 * *
 * * ---------------------- Revision History:----------------------
 * * <author>   <date>     <version>     <desc>
 * * linchaoyue 2020/9/15    1.0         create
 ******************************************************************/
public class BaseEntity<T> {
    int code;
    String msg;
    T data;

}
