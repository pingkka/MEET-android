package com.example.LastCapston.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.navigation.Navigation;

import com.example.LastCapston.R;
import com.example.LastCapston.databinding.FragmentCreateRoomBinding;
import com.example.LastCapston.main.CloudStorage;
import com.example.LastCapston.main.MainViewModel;


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


        /* 버튼클릭시 viewModel로 id정보를 넘겨주고 조건이 맞으면 방 생성 */
        binding.createBTN.setOnClickListener(v -> {
            viewModel.setTopic(binding.inputRoomID.getText().toString());
            viewModel.setName(binding.inputUserID.getText().toString());
            viewModel.clickSubmit();
            storage.checkRoomBeforeCreate(binding.inputRoomID.getText().toString(), binding.inputRoomPW.getText().toString(), binding.inputUserID.getText().toString());
        });

        /* 버튼 클릭시 홈 화면으로 이동 */
        binding.ereateExit.setOnClickListener(v -> {
            Navigation.findNavController(binding.getRoot()).navigate(R.id.action_createRoomFragment_to_homeFragment);
        });
        binding.createHomeButton.setOnClickListener(v -> {
            Navigation.findNavController(binding.getRoot()).navigate(R.id.action_createRoomFragment_to_homeFragment);
        });


        /* 방이 생성시 enterFlag값에 true를 넣어 화면 전환 */
        viewModel.enterFlag.observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                Navigation.findNavController(binding.getRoot()).navigate(R.id.action_createRoomFragment_to_chatRoomFragment);
            }
        });

        return binding.getRoot();
    }


}










