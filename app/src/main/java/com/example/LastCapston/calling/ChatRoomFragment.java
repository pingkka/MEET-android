package com.example.LastCapston.calling;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.LastCapston.R;
import com.example.LastCapston.databinding.FragmentChatRoomBinding;
import com.example.LastCapston.data.Code;
import com.example.LastCapston.data.MessageItem;
import com.example.LastCapston.data.SendText;
import com.example.LastCapston.data.UserItem;
import com.example.LastCapston.main.CloudStorage;

import com.example.LastCapston.adapter.ChatMessageAdapter;
import com.example.LastCapston.adapter.RecyclerViewAdapter;


import com.example.LastCapston.main.MQTTClient;
import com.example.LastCapston.main.MainViewModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;


import org.eclipse.paho.client.mqttv3.MqttException;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import lombok.SneakyThrows;

import static android.content.ContentValues.TAG;

public class ChatRoomFragment extends Fragment {
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FragmentChatRoomBinding binding = null;
    private MQTTClient client = MQTTClient.getInstance();
    private MainViewModel viewModel = MainViewModel.getInstance();
    private CallingViewModel callingViewModel;
    private CloudStorage storage = new CloudStorage(getActivity(), viewModel);

    //참여자 리스트
    private RecyclerView listView;
    private RecyclerViewAdapter adapter;

    //채팅 리스트
    private ArrayList<MessageItem> dataList;
    private ChatMessageAdapter chatMessageAdapter;
    private RecyclerView recyvlerv;

    private boolean observeTextFlag = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentChatRoomBinding.inflate(inflater, container, false);
        callingViewModel = new ViewModelProvider(this).get(CallingViewModel.class);

        //초기 참여자 목록 설정
        userListInit();

        //채팅
        chatRecyclerInit();
        client.publish(client.settingData.getTopic() + "/login", client.settingData.getUserName());


/* ----------------------------------    OnClickListener 함수        ---------------------------------------------------------------------------------*/
        //참여자 목록 확인하는 버튼
        binding.button.setOnClickListener(v -> {
            Toast.makeText(getActivity(), viewModel.getUserList().toString(), Toast.LENGTH_SHORT).show();
            client.publish(client.getTopic_text(), client.getUserName() + "&" +"good"+ "&" +"화남");
        });

        binding.btnMic.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    binding.btnMic.setImageResource(R.drawable.mic_on);
                    callingViewModel.touchMic();
                    break;
                case MotionEvent.ACTION_UP:
                    binding.btnMic.setImageResource(R.drawable.mic_off);
                    callingViewModel.touchMic();
                    break;

                default:
                    break;
            }
            return false;
        });

        //나가기 버튼 리스너
        binding.exit.setOnClickListener(v -> {
            Navigation.findNavController(binding.getRoot()).navigate(R.id.action_chatRoomFragment_to_homeFragment);
        });

/* -------------------------------------    observe 함수        ---------------------------------------------------------------------------------*/
        //참여자 목록 갱신
        viewModel.userListData.observe(getViewLifecycleOwner(), new Observer<ArrayList<UserItem>>() {
            @Override
            public void onChanged(ArrayList<UserItem> strings) {
                    userListUpdate();
            }
        });

        viewModel.loginUser.observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String s) {
                observeTextFlag = true;
                String user = viewModel.getLoginUser();
                SimpleDateFormat format = new SimpleDateFormat( "yyyy-MM-dd HH:mm");
                Date time = new Date();
                String timeString = format.format(time);
                dataList.add(new MessageItem(user + "님이 입장했습니다.", null,null, timeString, Code.ViewType.CENTER_CONTENT));
                recyvlerv.setAdapter(new ChatMessageAdapter(dataList));
                recyvlerv.scrollToPosition(dataList.size()-1);
            }
        });

        viewModel.logoutUser.observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String s) {
                String user = viewModel.getLogoutUser();
                SimpleDateFormat format = new SimpleDateFormat( "yyyy-MM-dd HH:mm");
                Date time = new Date();
                String timeString = format.format(time);
                dataList.add(new MessageItem(user + "님이 퇴장했습니다.", null, null,timeString, Code.ViewType.CENTER_CONTENT));
                recyvlerv.setAdapter(new ChatMessageAdapter(dataList));
                recyvlerv.scrollToPosition(dataList.size()-1);

            }
        });

        viewModel.currentText.observe(getViewLifecycleOwner(), new Observer<SendText>() {
            @Override
            public void onChanged(SendText sendText) {
                if(observeTextFlag){
                    String sendUser =  sendText.sendUser;
                    String text = sendText.sendText;
                    String image = sendText.sendImage;
                    addText(sendUser, text, image);
                }
            }
        });
        return binding.getRoot();
    }


    /* --------------------------------------------   그 외 함수        ---------------------------------------------------------------------------------*/

    //참여자 목록 생성 함수
    public void userListInit(){
        listView = binding.userListView;
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity(),  LinearLayoutManager.HORIZONTAL, false);
        listView.setLayoutManager(layoutManager);

        adapter = new RecyclerViewAdapter(getActivity(), viewModel);
        listView.setAdapter(adapter);

        UserListDecoration decoration = new UserListDecoration();
        listView.addItemDecoration(decoration);
    }
    //참여자 목록 UPDATE함수
    private void userListUpdate(){

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity(),  LinearLayoutManager.HORIZONTAL, false);
        listView.setLayoutManager(layoutManager);
        adapter = new RecyclerViewAdapter(getActivity(), viewModel);
        listView.setAdapter(adapter);

        UserListDecoration decoration = new UserListDecoration();
        listView.addItemDecoration(decoration);
    }

    // 채팅창recyclerView 생성 함수
    private void chatRecyclerInit(){
        dataList = new ArrayList();
        recyvlerv = binding.recyvlerv;
        LinearLayoutManager manager = new LinearLayoutManager(getActivity(), RecyclerView.VERTICAL, false);
        recyvlerv.setLayoutManager(manager);
        recyvlerv.setAdapter(new ChatMessageAdapter(dataList));
    }


    // 채팅창 text 추가 함수
    public void addText(String user, String text, String image){

        if(client.getUserName().equals(user)){
            SimpleDateFormat format = new SimpleDateFormat( "yyyy-MM-dd HH:mm");
            Date time = new Date();
            String timeString = format.format(time);
            dataList.add(new MessageItem(text, null, image, timeString,Code.ViewType.RIGHT_CONTENT));
            recyvlerv.setAdapter(new ChatMessageAdapter(dataList));
            recyvlerv.scrollToPosition(dataList.size()-1);
        }else{
            SimpleDateFormat format = new SimpleDateFormat( "yyyy-MM-dd HH:mm");
            Date time = new Date();
            String timeString = format.format(time);
            dataList.add(new MessageItem(text, user, image, timeString, Code.ViewType.LEFT_CONTENT));
            recyvlerv.setAdapter(new ChatMessageAdapter(dataList));
            recyvlerv.scrollToPosition(dataList.size()-1);
        }

    }



    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @SneakyThrows
    @Override
    public void onDestroy() {
        super.onDestroy();
        String roomID = client.settingData.getTopic();
        String user = client.settingData.getUserName();
        System.out.println(viewModel.getUserList().toString());
        client.publish(roomID+"/logout", user);
        ArrayList<String> userList = viewModel.getUserList();
        logout();
        /* firebase 참여자목록 삭제, mqtt 삭제 초기화*/
        databaseLogout(userList, roomID, user);

        /* MQTTClient 연결 해제 */

        client.getParticipantsList().clear();
        client.getConnectOptions().setAutomaticReconnect(false);

        /* view모델 초기화 */
        //MQTTSettingData 초기화
        viewModel.initMQTTSettingData();
        //mainViewmodel 초기화
        viewModel.mainViewMoedlInit();
    }

    private void logout(){
        /* 이전의 출력된 텍스트 지우기 */
        dataList.clear();

        client.getConnectOptions().setAutomaticReconnect(false);
        /* Audio 관련 처리 */
        /* -------------------------- 추가 ------------------------- */
        /* EmotionThread interrupt */
        if (callingViewModel.getEmotionFlag()) {
            callingViewModel.getEmotionThread().setEmotionFlag(false);
        }
        callingViewModel.getEmotionThread().interrupt();

        /* SttThread interrupt */
        if(callingViewModel.getSttFlag()) {
            callingViewModel.getSttThread().setSttFlag(false);
        }
        callingViewModel.getSttThread().interrupt();
        /* -------------------------------------------------------- */

        /* Audio 관련 처리 */
        /* RecordThread interrupt */
        if(callingViewModel.getRecordFlag()) {
            callingViewModel.getRecordThread().setRecordFlag(false);
        }

        callingViewModel.getRecordThread().stopRecording();
        callingViewModel.getRecordThread().interrupt();



        /* topic_audio unsubscribe */
        if(client.getClient().isConnected()) {
            try {
                client.getClient().unsubscribe(client.getTopic_audio());
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }

        /* PlayThreadList의 모든 PlayThread interrupt 및 PlayThreadList 초기화*/
        //java.util.ConcurrentModificationException 원인 및 처리 방법
        //index가 실시간으로 변하기 때문에 발생하는 오류
        for(Iterator<PlayThread> itr = client.getPlayThreadList().iterator(); itr.hasNext();){
            PlayThread playThread = itr.next();
            if(callingViewModel.getPlayFlag()) {
                playThread.setPlayFlag(false);
            }

            playThread.stopPlaying();
            synchronized (playThread.getAudioQueue()) {
                playThread.getAudioQueue().clear();
            }
            playThread.interrupt();
        }
//        for(PlayThread playThread : client.getPlayThreadList()) {
//            if(callingViewModel.getPlayFlag()) {
//                playThread.setPlayFlag(false);
//            }
//
//            playThread.stopPlaying();
//            synchronized (playThread.getAudioQueue()) {
//                playThread.getAudioQueue().clear();
//            }
//            playThread.interrupt();
//        }

        client.getPlayThreadList().clear();

        //실제 연결 끊음



        /* MQTTClient 연결 해제 */
        client.getParticipantsList().clear();
        client.disconnect();
        client.getConnectOptions().setAutomaticReconnect(false);
    }


    /* 로그아웃시 firebase에서 삭제해주는 함수 */
    public void databaseLogout(ArrayList<String> userList, String roomID, String user) throws Exception {
        System.out.println(userList.size());

        DocumentReference docRef = db.collection("rooms").document(roomID);
        if (userList.size() == 1) {
            docRef
                    .delete()
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.w(TAG, "사람 없음");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w(TAG, "로그아웃 방 삭제 실패", e);
                        }
                    });
        } else {
            // Remove the 'capital' field from the document
            Map<String, Object> updates = new HashMap<>();
            updates.put("participants", FieldValue.arrayRemove(user));
            docRef.update(updates).addOnCompleteListener(new OnCompleteListener<Void>() {

                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    Log.w(TAG, "아직 방에 사람 있음");


                }
            });
        }

    }
}