package com.example.rxjava2demo;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.functions.Function;


public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.tv_dailog).setOnClickListener(v -> code2Dialog());
    }

    @SuppressLint("CheckResult")
    public void testCompose() {
        Observable.create((ObservableOnSubscribe<Integer>) e -> {
            e.onNext(1);
            e.onNext(2);
            e.onComplete();
        }).compose(RxThreadUtils.observableToMain())
                .subscribe(i -> System.out.println("onNext : i= " + i));
    }


    @SuppressLint("CheckResult")
    public void code2Error() {
        Observable.create((ObservableOnSubscribe<BaseEntity<UserInfo>>) e -> {
            e.onError(new JSONException("test"));
        }).flatMap((Function<BaseEntity<UserInfo>, ObservableSource<BaseEntity<UserInfo>>>) userInfoBaseEntity -> {
            if (userInfoBaseEntity.code == -10) {//token异常需要登录
                return Observable.error(new JSONException("TEST"));
            }
            return Observable.just(userInfoBaseEntity);
        }).subscribe(userInfoBaseEntity -> {

        }, throwable -> {
            if (throwable instanceof JSONException) {
                //跳转到登录页
            }
        });
    }

    @SuppressLint("CheckResult")
    public void code2Dialog() {
        Observable.create((ObservableOnSubscribe<BaseEntity<UserInfo>>) e -> {
            e.onError(new JSONException("test"));
        }).onErrorResumeNext(throwable -> {
            if (throwable instanceof JSONException) {
                return Observable.error(new JSONException("DDD"));
            }
            return Observable.error(throwable);
        }).retryWhen(throwableObservable -> throwableObservable.flatMap(throwable -> {
            if (throwable instanceof JSONException) {
                return RxDialogUtils.showErrorDialog(MainActivity.this, "").flatMapObservable((Function<Boolean, ObservableSource<?>>) aBoolean -> {
                    if (aBoolean) {
                        //1s后重试
                        return Observable.timer(1000, TimeUnit.MILLISECONDS);
                    } else {
                        return Observable.error(throwable);
                    }
                });
            } else {
                return Observable.error(throwable);
            }
        })).subscribe(userInfoBaseEntity -> {

        }, throwable -> {

        });
    }

}