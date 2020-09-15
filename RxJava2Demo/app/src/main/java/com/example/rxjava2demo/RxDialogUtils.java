package com.example.rxjava2demo;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

/*****************************************************************
 * * File: - RxDialogUtils
 * * Description: 
 * * Version: 1.0
 * * Date : 2020/9/14
 * * Author: linchaoyue
 * *
 * * ---------------------- Revision History:----------------------
 * * <author>   <date>     <version>     <desc>
 * * linchaoyue 2020/9/14    1.0         create
 ******************************************************************/
public class RxDialogUtils {
    public static Single<Boolean> showErrorDialog(Context context, String message) {
        return Single.create((SingleOnSubscribe<Boolean>) e ->
                new AlertDialog.Builder(context)
                        .setTitle("错误")
                        .setMessage("您收到了一个异常,是否重试本次请求？")
                        .setCancelable(false).setPositiveButton("确定", (dialog, which) ->
                        e.onSuccess(true))
                        .setNegativeButton("取消", (dialog, which) ->
                                e.onSuccess(false))
                        .show()).subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(Schedulers.io());
    }


}
