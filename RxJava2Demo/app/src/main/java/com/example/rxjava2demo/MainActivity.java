package com.example.rxjava2demo;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Lifecycle;


import com.trello.lifecycle2.android.lifecycle.AndroidLifecycle;
import com.trello.rxlifecycle3.LifecycleProvider;


import org.json.JSONException;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.ObservableTransformer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;


public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.tv_dailog).setOnClickListener(v -> code2Dialog());
        LifecycleProvider<Lifecycle.Event> provider
                = AndroidLifecycle.createLifecycleProvider(this);
        Observable.interval(3, 2, TimeUnit.SECONDS)
                .doOnDispose(new Action() {
                    @Override
                    public void run() throws Exception {
                        Log.d(TAG, "解除了订阅");
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .compose(provider.<Long>bindToLifecycle())
                .subscribe((Consumer<Long>) aLong -> {
                    //在onCreate()中启动，一直运行到onDestory()
                    Log.d(TAG, +aLong + "");
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG,  "onDestroy");
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

    @SuppressLint("CheckResult")
    public void testTransform() {
        Observable.just(123, 456)
                .compose(transformer())
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(@io.reactivex.annotations.NonNull String s) throws Exception {
                        System.out.println("s=" + s);
                    }
                });

    }

    public static <String> ObservableTransformer<Integer, java.lang.String> transformer() {
        return new ObservableTransformer<Integer, java.lang.String>() {
            @Override
            public ObservableSource<java.lang.String> apply(@NonNull Observable<Integer> upstream) {
                return upstream.map(new Function<Integer, java.lang.String>() {
                    @Override
                    public java.lang.String apply(@NonNull Integer integer) throws Exception {
                        return java.lang.String.valueOf(integer);
                    }
                });
            }
        };
    }


}