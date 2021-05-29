package com.example.last_capston.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.last_capston.CloudStorage;
import com.example.last_capston.Code;
import com.example.last_capston.MessageItem;
import com.example.last_capston.R;
import com.example.last_capston.UserListDecoration;
import com.example.last_capston.adapter.ChatMessageAdapter;
import com.example.last_capston.adapter.RecyclerViewAdapter;
import com.example.last_capston.calling.CallingViewModel;
import com.example.last_capston.calling.PlayThread;

import com.example.last_capston.databinding.FragmentChatRoomBinding;
import com.example.last_capston.main.MQTTClient;
import com.example.last_capston.main.MainViewModel;

import org.eclipse.paho.client.mqttv3.MqttException;

import java.util.ArrayList;

import lombok.SneakyThrows;

public class ChatRoomFragment extends Fragment {
    private FragmentChatRoomBinding binding = null;
    private MQTTClient client = MQTTClient.getInstance();
    private MainViewModel viewModel = MainViewModel.getInstance();;
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




        binding.btnMic.setOnClickListener(v -> {
            if (callingViewModel.clickMic()) {
                binding.btnMic.setText("mic true");
            } else {
                binding.btnMic.setText("mic false");
            }
        });

        //초기 참여자 목록 설정
        init();
        //참여자 목록 확인하는 버튼
        binding.button.setOnClickListener(v -> {
            Toast.makeText(getActivity(), viewModel.getUserList().toString(), Toast.LENGTH_SHORT).show();
        });

        //채팅
        initData();
        recyvlerv = binding.recyvlerv;
        LinearLayoutManager manager = new LinearLayoutManager(getActivity(), RecyclerView.VERTICAL, false);
        recyvlerv.setLayoutManager(manager);
        recyvlerv.setAdapter(new ChatMessageAdapter(dataList));

        //나가기 버튼 리스너
        binding.exit.setOnClickListener(v -> {
            Navigation.findNavController(binding.getRoot()).navigate(R.id.action_chatRoomFragment_to_homeFragment);
        });
        
        
        //참여자 목록 갱신
        viewModel.userListLivedata.observe(getViewLifecycleOwner(), new Observer<ArrayList<String>>() {
            @Override
            public void onChanged(ArrayList<String> strings) {
                ArrayList<String> list = viewModel.getUserList();
                userList();
            }
        });

        viewModel.loginUser.observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String s) {
                String user = viewModel.getLoginUser();
                dataList.add(new MessageItem(user + "님이 입장했습니다.", null, Code.ViewType.CENTER_CONTENT));
                recyvlerv.setAdapter(new ChatMessageAdapter(dataList));
            }
        });

        viewModel.logoutUser.observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String s) {
                String user = viewModel.getLogoutUser();
                dataList.add(new MessageItem(user + "님이 퇴장했습니다.", null, Code.ViewType.CENTER_CONTENT));
                recyvlerv.setAdapter(new ChatMessageAdapter(dataList));
            }


        });



        return binding.getRoot();


    }




//참여자 목록 live data로 참여,퇴장을 확인한후 추가 및 제거
    public void init(){
        listView = binding.userLv;
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity(),  LinearLayoutManager.HORIZONTAL, false);
        listView.setLayoutManager(layoutManager);
        ArrayList<String> itemList = new ArrayList<>();
        itemList = viewModel.getUserList();
        adapter = new RecyclerViewAdapter(getActivity(), itemList, onClickItem);
        listView.setAdapter(adapter);

        UserListDecoration decoration = new UserListDecoration();
        listView.addItemDecoration(decoration);
    }

    private void userList(){
        listView = binding.userLv;
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity(),  LinearLayoutManager.HORIZONTAL, false);
        listView.setLayoutManager(layoutManager);
        adapter = new RecyclerViewAdapter(getActivity(), viewModel.userList, onClickItem);
        listView.setAdapter(adapter);

        UserListDecoration decoration = new UserListDecoration();
        listView.addItemDecoration(decoration);
    }

    // 입장 퇴장 알림
    private void initData(){
        dataList = new ArrayList();
    }

    private View.OnClickListener onClickItem = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String str = (String) v.getTag();
            Toast.makeText(getActivity(), str, Toast.LENGTH_SHORT).show();
        }
    };




    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @SneakyThrows
    @Override
    public void onDestroy() {
        super.onDestroy();

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


        // firebase 의 연결을 끊고 마지막으로 mqtt도 끊는다.
        String roomID = client.settingData.getTopic();
        String user = client.settingData.getUserName();
        System.out.println(viewModel.getUserList().toString());
        storage.logout(roomID, user);


    }

}

