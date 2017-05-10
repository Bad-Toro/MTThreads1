package com.mobileappscompany.training.mtthreads1;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.concurrent.Callable;

import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {
    TextView tV;
    Subscription myS; // For RxJava
    IntentFilter iF;  // For BroadcastReceiver
    BroadcastReceiver bR;  // For BroadcastReceiver


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tV = (TextView) findViewById(R.id.textView);

    }



    //////////////////////////////// Thread /////////////////////////////////


    public void onThread(View view) {
        new Thread(){
            public void run(){
                try {
                    Thread.sleep(4000);
                    tV.post(new Runnable() {
                        @Override
                        public void run() {
                            tV.setText("Done with Thread");
                        }
                    });
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }


    ////////////////////////// Thread /////////////////////////////
    ////////////////////////// asyncTask //////////////////////

    public void onAT(View view) {
        new AsyncTask<Integer, Void, String>() {
            @Override
            protected String doInBackground(Integer[] params) {

                int tTS = params[0] *1000;

                try {
                    Thread.sleep(tTS);
                    return "Done with AsyncTask " + tTS;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    return "AsyncTask Error";
                }
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                tV.setText(s);
            }
        }.execute(3);
    }



    ////////////////////////// asyncTask //////////////////////
    ////////////////////////// EventBus //////////////////////

    public class MyEvent{
        private String message;
        private int messageCode;
//
//        public MyEvent(String message) {
//            this.message = message;
//        }

        public MyEvent(String message, int messageCode) {
            this.message = message;
            this.messageCode = messageCode;
        }

        public String getMessage() {
            return message;
        }

        public int getMessageCode() {
            return messageCode;
        }
    }


    public void onEventBus(View view) {

        new Thread(){
            public void run(){
                try {
                    Thread.sleep(3000);
                    EventBus.getDefault().post(new MyEvent("Done with EventBus",747));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }.start();

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void myEvHdlr(MyEvent e){
        if (e.getMessageCode()==747) {
            tV.setText(e.getMessage());
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
        unregisterReceiver(bR); //For BR
    }

    ////////////////////////// EventBus //////////////////////
    ////////////////////////// RxJava //////////////////////


    public void onRxJava(View view) {
        Observable<Integer> myO = Observable.fromCallable(
                new Callable<Integer>() {
                    @Override
                    public Integer call() throws InterruptedException {
                        Thread.sleep(4000);
                        return 4;
                    }});
        myS = myO.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Integer>() {
                    @Override
                    public void onCompleted() {
                    }
                    @Override
                    public void onError(Throwable e) {
                    }
                    @Override
                    public void onNext(Integer i) {
                        tV.setText("Done RxJava : " + i);
                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(!myS.isUnsubscribed()){
            myS.unsubscribe();
        }
    }

    ////////////////////////// RxJava //////////////////////
    ////////////////////////// BR //////////////////////



    public void onBR(View view) {

        iF = new IntentFilter();
        iF.addAction("com.mobileappscompany.training.mtthreads1.MyAction");

        bR = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                tV.setText("Done with BR");
            }
        };

        registerReceiver(bR,iF);

        new Thread(){
            public void run(){
                try {
                    Thread.sleep(3000);
                    Intent i = new Intent();
                    i.setAction("com.mobileappscompany.training.mtthreads1.MyAction");
                    sendBroadcast(i);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }.start();



    }



    ////////////////////////// BR //////////////////////
}
