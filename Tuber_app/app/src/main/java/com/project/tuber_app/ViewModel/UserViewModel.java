package com.project.tuber_app.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.room.Room;

import com.project.tuber_app.databases.UserDao;
import com.project.tuber_app.databases.UserEntity;

public class UserViewModel extends AndroidViewModel {


    public UserViewModel(@NonNull Application application) {
        super(application);

    }

}
