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
import com.example.LastCapston.databinding.FragmentCreateRoomBinding;
import com.example.LastCapston.main.MQTTClient;
import com.example.LastCapston.main.MainViewModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;


public class CreateRoomFragment extends Fragment{

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FragmentCreateRoomBinding binding = null;
    private MainViewModel viewModel;
    private MQTTClient client = MQTTClient.getInstance();
    private MQTTSettingData settingData = MQTTSettingData.getInstance();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentCreateRoomBinding.inflate(inflater, container, false);
        viewModel = MainViewModel.getInstance();

        /* 버튼클릭시 viewModel로 id정보를 넘겨주고 조건이 맞으면 방 생성 */
        binding.createBTN.setOnClickListener(v -> {
            checkRoomBeforeCreate(binding.roomID.getText().toString(), binding.roomPW.getText().toString(), binding.userID.getText().toString());
        });

        /* 버튼 클릭시 홈 화면으로 이동 */
        binding.ereateExit.setOnClickListener(v -> {
            Navigation.findNavController(binding.getRoot()).navigate(R.id.action_createRoomFragment_to_homeFragment);
        });
        binding.createHomeButton.setOnClickListener(v -> {
            Navigation.findNavController(binding.getRoot()).navigate(R.id.action_createRoomFragment_to_homeFragment);
        });

        return binding.getRoot();
    }

    /* 방 생성 버튼 클릭시 실행되는 코드(입장 하기전 이미 방이 있는지, 아이디,비밀번호가 올바른지 확인)  */
    private void checkRoomBeforeCreate(String roomID, String roomPW, String user) {

        //아이디, 비밀번호, 방이름이 공백인지 확인한다
        if (!checkLogin(roomID, roomPW, user)) {
            return;
        }

        //firebase에서 ID검사
        DocumentReference docRef = db.collection("rooms").document(roomID);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Toast.makeText(getActivity(), "이미 동일한 이름의 방이 있습니다. ", Toast.LENGTH_SHORT).show();
                    } else {

                        //다 올바르게 수행하였으면 화면전환

                        //firebase에 방 생성
                        storeDB(roomID, roomPW, user);
                        // 화면전환을 먼저 하는 이유 chatRoomFragment 에는 livedata가 있는데 값 변경뿐 아니라 화면이 나타날때 도 호출이됨

                        //viewModel 설정
                        viewModel.setTopic(binding.roomID.getText().toString());
                        viewModel.setName(binding.userID.getText().toString());
                        viewModel.clickSubmit();//(설정이 완료된 ip, port, topic, name을 setting data에 저장하는 함수)

                        //올바르게 입장 하였으면 그에 맞게 mqtt설정
                        initMQTT();
                        Navigation.findNavController(binding.getRoot()).navigate(R.id.action_createRoomFragment_to_chatRoomFragment);

                    }
                } else {
                    Log.d("tag", "get failed with ", task.getException());
                }
            }
        });
    }

    private void storeDB(String roomID, String roomPW, String participant) {
        CollectionReference rooms = db.collection("rooms");
        Map<String, Object> user = new HashMap<>();
        user.put("roomeID", roomID);
        user.put("roomPW", roomPW);
        user.put("participants", Arrays.asList(participant));
        rooms.document(roomID).set(user);
        Toast.makeText(getActivity(), "방을 생성했습니다.", Toast.LENGTH_SHORT).show();
    }

    private void initMQTT() {
        //mqtt sub
        client.init(settingData.getTopic(), settingData.getUserName(), settingData.getIp(), settingData.getPort(),  viewModel);
        client.subscribeAll();
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

}