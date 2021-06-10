package com.example.LastCapston.calling;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

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


import org.eclipse.paho.client.mqttv3.MqttException;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import lombok.SneakyThrows;

public class ChatRoomFragment extends Fragment {

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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentChatRoomBinding.inflate(inflater, container, false);
        callingViewModel = new ViewModelProvider(this).get(CallingViewModel.class);

        //초기 참여자 목록 설정
        init();

        //채팅
        initData();



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
            viewModel.setEnterFlag(false);
            Navigation.findNavController(binding.getRoot()).navigate(R.id.action_chatRoomFragment_to_homeFragment);
        });




/* -------------------------------------    observe 함수        ---------------------------------------------------------------------------------*/
        //참여자 목록 갱신
        viewModel.userListData.observe(getViewLifecycleOwner(), new Observer<ArrayList<UserItem>>() {
            @Override
            public void onChanged(ArrayList<UserItem> strings) {
                userList();
            }
        });

        viewModel.loginUser.observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String s) {
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
                String sendUser =  sendText.sendUser;
                String text = sendText.sendText;
                String image = sendText.sendImage;
                addText(sendUser, text, image);
            }

        });

        return binding.getRoot();
    }



/* --------------------------------------------   그 외 함수        ---------------------------------------------------------------------------------*/

    //참여자 목록 초기화 함수
    public void init(){
        listView = binding.userListView;
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity(),  LinearLayoutManager.HORIZONTAL, false);
        listView.setLayoutManager(layoutManager);

        adapter = new RecyclerViewAdapter(getActivity(), viewModel);
        listView.setAdapter(adapter);

        UserListDecoration decoration = new UserListDecoration();
        listView.addItemDecoration(decoration);
    }
    //참여자 목록 UPDATE함수
    private void userList(){

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity(),  LinearLayoutManager.HORIZONTAL, false);
        listView.setLayoutManager(layoutManager);
        adapter = new RecyclerViewAdapter(getActivity(), viewModel);
        listView.setAdapter(adapter);

        UserListDecoration decoration = new UserListDecoration();
        listView.addItemDecoration(decoration);
    }

    // 채팅창recyclerView  초기화 함수
    private void initData(){

        dataList = new ArrayList();
        recyvlerv = binding.recyvlerv;
        LinearLayoutManager manager = new LinearLayoutManager(getActivity(), RecyclerView.VERTICAL, false);
        recyvlerv.setLayoutManager(manager);
        recyvlerv.setAdapter(new ChatMessageAdapter(dataList));

    }


    // 채팅창 TEXT추가 함수
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
            dataList.add(new MessageItem(text, null, image, timeString, Code.ViewType.LEFT_CONTENT));
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
        for(PlayThread playThread : client.getPlayThreadList()) {
            if(callingViewModel.getPlayFlag()) {
                playThread.setPlayFlag(false);
            }

            playThread.stopPlaying();
            synchronized (playThread.getAudioQueue()) {
                playThread.getAudioQueue().clear();
            }
            playThread.interrupt();
        }

        client.getPlayThreadList().clear();

        //실제 연결 끊음

        /* firebase 참여자목록 삭제, mqtt 삭제 초기화*/
        String roomID = client.settingData.getTopic();
        String user = client.settingData.getUserName();
        System.out.println(viewModel.getUserList().toString());
        storage.logout(roomID, user);
    }
}

//    private View.OnClickListener onClickItem = new View.OnClickListener() {
//        @Override
//        public void onClick(View v) {
//            String str = (String) v.getTag();
//            Toast.makeText(getActivity(), str, Toast.LENGTH_SHORT).show();
//        }
//    };
