package com.example.kageboshi.contacts_debug.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.kageboshi.contacts_debug.R;
import com.example.kageboshi.contacts_debug.adapter.ContactAdapter;
import com.example.kageboshi.contacts_debug.http.RetrofitFactory;
import com.example.kageboshi.contacts_debug.http.model.ContactResponseModel;
import com.example.kageboshi.contacts_debug.utils.Constants;
import com.example.kageboshi.contacts_debug.utils.ContactsUtils;
import com.example.kageboshi.contacts_debug.utils.ToastUtil;

import java.util.List;

import io.reactivex.Observer;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class ContactActivity extends AppCompatActivity implements View.OnClickListener {

    private String token;
    private Toolbar toolbarContact;
    private RecyclerView recyclerContacts;
    private Button buttonDownload;
    private Button buttonClear;
    private TextView textTitle;
    private List<ContactResponseModel.DataBean.ContactsBean> contactsList;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact);
        Intent intent = getIntent();
        token = intent.getStringExtra(Constants.TOKEN_KEY);
        //  Log.e("TAG",string);
        initView();
        toolbarSetting();
        permissionCheck();
    }


    private void permissionCheck() {
        if (checkSelfPermission(Manifest.permission_group.CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.WRITE_CONTACTS, Manifest.permission.READ_CONTACTS}, Constants.CONTACTS_REQUEST_CODE);
            return;
        }
    }


    private void toolbarSetting() {
        toolbarContact.setTitle("");
        setSupportActionBar(toolbarContact);
        textTitle = ((TextView) toolbarContact.findViewById(R.id.textTitle));
        textTitle.setText(getResources().getText(R.string.contacts_Activity));
    }


    private void initView() {
        toolbarContact = ((Toolbar) findViewById(R.id.toolbar_contact));
        recyclerContacts = ((RecyclerView) findViewById(R.id.recycler_contacts));
        buttonDownload = ((Button) findViewById(R.id.download));
        buttonClear = ((Button) findViewById(R.id.clear));
        buttonClear.setOnClickListener(this);
        buttonDownload.setOnClickListener(this);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == Constants.CONTACTS_REQUEST_CODE) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED || grantResults[1] != PackageManager.PERMISSION_GRANTED) {
                ToastUtil.show(this, R.string.permission_denied);
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.download:
                downloadContactInfo();
                break;
            case R.id.clear:
                ContactsUtils.deleteAll(getApplicationContext());
                ToastUtil.show(getApplicationContext(), R.string.contacts_clear);
                recyclerContacts.setVisibility(View.INVISIBLE);
                break;
        }
    }


    private void downloadContactInfo() {
        RetrofitFactory.getInstance().getContacts(token, "0", "0", 5)
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<ContactResponseModel>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(ContactResponseModel contactResponseModel) {
                        Log.e("TAG", "SUCCESS");
                        contactsList = contactResponseModel.getData().getContacts();
                        if (contactsList.size() > 0) {
                            writeintoPhone();
                            recyclerContacts.setVisibility(View.VISIBLE);
                            showContacts();
                            ToastUtil.show(getApplicationContext(), R.string.contacts_downloaded);
                        }

                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e("TAG", "ERROR");
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    private void showContacts() {
        recyclerContacts.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        ContactAdapter adapter = new ContactAdapter(getApplicationContext(), contactsList);
        recyclerContacts.setAdapter(adapter);
    }

    private void writeintoPhone() {
        for (int i = 0; i < contactsList.size(); i++) {
            String name = contactsList.get(i).getName();
            String phone = contactsList.get(i).getPhone();
            ContactsUtils.addContact(getApplicationContext(), name, phone);
        }
    }
}
