package com.example.LastCapston.home;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.LastCapston.R;
import com.example.LastCapston.data.MQTTSettingData;
import com.example.LastCapston.databinding.FragmentEnterRoomBinding;
import com.example.LastCapston.main.MQTTClient;

import com.example.LastCapston.main.MainViewModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

import static android.content.ContentValues.TAG;


public class EnterRoomFragment extends Fragment {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FragmentEnterRoomBinding binding = null;
    private MainViewModel viewModel;
    private MQTTClient client = MQTTClient.getInstance();
    private MQTTSettingData settingData = MQTTSettingData.getInstance();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentEnterRoomBinding.inflate(inflater, container, false);
        viewModel = MainViewModel.getInstance();

        /* 버튼클릭시 viewModel로 id정보를 넘겨주고 조건이 맞으면 방 입장 */
        binding.enterOK.setOnClickListener(v -> {

            checkRoomBeforeEnter(binding.roomID.getText().toString(), binding.roomPW.getText().toString(), binding.userID.getText().toString());
        });

        /* 버튼 클릭시 홈 화면으로 이동 */
        binding.enterExit.setOnClickListener(v -> {
            Navigation.findNavController(binding.getRoot()).navigate(R.id.action_enterRoomFragment_to_homeFragment);
        });
        binding.enterHomeButton.setOnClickListener(v -> {
            Navigation.findNavController(binding.getRoot()).navigate(R.id.action_enterRoomFragment_to_homeFragment);
        });

        return binding.getRoot();
    }
    public void checkRoomBeforeEnter(String roomID, String roomPW, String user) {
        checkLogin(roomID, roomPW, user);
        DocumentReference docRef = db.collection("rooms").document(roomID);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    ArrayList<String> getparticipants = new ArrayList<String>();
                    if (document.exists()) {
                        getparticipants = (ArrayList<String>) document.getData().get("participants");
                    }
                    if (!document.exists()) {
                        Toast.makeText(getActivity(), "room 없음 ", Toast.LENGTH_SHORT).show();
                    } else if (!document.getData().get("roomPW").toString().equals(roomPW)) {
                        Toast.makeText(getActivity(), "비번 틀림", Toast.LENGTH_SHORT).show();
                    } else if (getparticipants.contains(user) == true) {
                        Toast.makeText(getActivity(), "같은 이름 있음", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getActivity(), "저장 성공", Toast.LENGTH_SHORT).show();
                        DocumentReference Ref = db.collection("rooms").document(roomID);
                        Ref.update("participants", FieldValue.arrayUnion(user));
                        try {
                            //viewModel 설정
                            viewModel.setTopic(binding.roomID.getText().toString());
                            viewModel.setName(binding.userID.getText().toString());
                            viewModel.clickSubmit();

                            //mqtt sub
                            initMQTT();

                            Navigation.findNavController(binding.getRoot()).navigate(R.id.action_enterRoomFragment_to_chatRoomFragment);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
    }

    /* 로그인시 공백을 검사하는 함수 */
    public boolean checkLogin(String roomID, String roomPW, String user) {
        if (roomID.equals("")) {
            Toast.makeText(getActivity(), "roomID를 작성해주세요. ", Toast.LENGTH_SHORT).show();
            return false;
        } else if (roomPW.equals("")) {
            Toast.makeText(getActivity(), "roomPW를 작성해주세요.", Toast.LENGTH_SHORT).show();
            return false;
        } else if (user.equals("")) {
            Toast.makeText(getActivity(), "사용자ID를 작성해주세요.", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void initMQTT() {
        //mqtt sub
        client.init(settingData.getTopic(), settingData.getUserName(), settingData.getIp(), settingData.getPort(), viewModel);
        client.subscribeAll();
    }
}