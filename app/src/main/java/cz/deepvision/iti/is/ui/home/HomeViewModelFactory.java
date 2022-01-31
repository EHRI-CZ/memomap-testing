package cz.deepvision.iti.is.ui.home;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import cz.deepvision.iti.is.models.Location;

public class HomeViewModelFactory implements ViewModelProvider.Factory {
    private Application mApplication;
    private Location mParam;


    public HomeViewModelFactory(Application application, Location param) {
        mApplication = application;
        mParam = param;
    }


    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new HomeViewModel(mApplication,mParam);
    }
}
