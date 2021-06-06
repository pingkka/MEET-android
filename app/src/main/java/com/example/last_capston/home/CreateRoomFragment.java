package com.example.last_capston.home;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.navigation.Navigation;

import com.example.last_capston.main.CloudStorage;
import com.example.last_capston.R;
import com.example.last_capston.databinding.FragmentCreateRoomBinding;
import com.example.last_capston.main.MainViewModel;

import java.util.ArrayList;

import static android.content.Context.INPUT_METHOD_SERVICE;

public class CreateRoomFragment extends Fragment{

    private FragmentCreateRoomBinding binding = null;
    private MainViewModel viewModel;
    private CloudStorage storage;
    private InputMethodManager imm;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentCreateRoomBinding.inflate(inflater, container, false);
        viewModel = MainViewModel.getInstance();
        storage = new CloudStorage(getActivity(), viewModel);
        // 입력받는 방법을 관리하는 Manager객체를  요청하여 InputMethodmanager에 반환한다.



        binding.createBTN.setOnClickListener(v -> {
            viewModel.setTopic(binding.inputRoomID.getText().toString());
            viewModel.setName(binding.inputUserID.getText().toString());
            viewModel.clickSubmit();
            storage.checkRoomBeforeCreate(binding.inputRoomID.getText().toString(), binding.inputRoomPW.getText().toString(), binding.inputUserID.getText().toString());
        });

        binding.ereateExit.setOnClickListener(v -> {
            Navigation.findNavController(binding.getRoot()).navigate(R.id.action_createRoomFragment_to_homeFragment);
        });

        binding.createHomeButton.setOnClickListener(v -> {
            Navigation.findNavController(binding.getRoot()).navigate(R.id.action_createRoomFragment_to_homeFragment);
        });


        viewModel.enterFlag.observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                Navigation.findNavController(binding.getRoot()).navigate(R.id.action_createRoomFragment_to_chatRoomFragment);
            }
        });

        return binding.getRoot();
    }





//    @Override
//    public void onClick(View view) {
//        switch (view.getId()) {
//            case R.id.inputRoomID:
//                // 키보드를 보여준다.
//                // 첫번째 매개변수에 해당하는(여기서는 input) 곳에 포커스가 주어진다.
//                // 두번째 매개변수에는 해당 Flag없이 0을 넘겨준다.
//                // 주의 : 첫번째 매개변수가 다른 EditText에 포커스 되어있다면 키보드가 보여지지 않는다.
//                imm.showSoftInput(input1, 0);
//                break;
//            case R.id.hide:
//                // 키보드를 숨겨준다.
//                // 첫번째 매개변수에 해당하는(여기서는 input) 곳에 키보드가 보이면 키보드를 숨긴다
//                // 두번째 매개변수에는 해당 Flag없이 0을 넘겨준다.
//                imm.hideSoftInputFromWindow(input1.getWindowToken(), 0);
//                break;
//        }
//    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Toast.makeText(getActivity(), "onDestroyView", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Toast.makeText(getActivity(), "onDestroy", Toast.LENGTH_SHORT).show();


    }
}










