package com.example.LastCapston.calling;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.LastCapston.R;
import com.example.LastCapston.adapter.ChatMessageAdapter;
import com.example.LastCapston.adapter.RecyclerViewAdapter;
import com.example.LastCapston.data.Code;
import com.example.LastCapston.data.MessageItem;
import com.example.LastCapston.data.SendText;
import com.example.LastCapston.data.UserItem;
import com.example.LastCapston.data.UserSpeakState;
import com.example.LastCapston.databinding.FragmentChatRoomBinding;
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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
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

    //참여자 리스트
    private RecyclerView listView;
    private RecyclerViewAdapter adapter;
    private LinearLayoutManager layoutManager;

    //채팅 리스트
    private ArrayList<MessageItem> dataList;
    private ChatMessageAdapter chatMessageAdapter;
    private RecyclerView recyvlerv;

    private boolean observeTextFlag = false;

    /* 대화 내용 저장 관련 변수 */
    private Boolean autoSaveFlag = false;
    private File msgDir;
    private File msgFile;
    private String msgFileName;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentChatRoomBinding.inflate(inflater, container, false);
        callingViewModel = new ViewModelProvider(this).get(CallingViewModel.class);

        binding.roomName.setText(callingViewModel.getTopic());

        autoSaveFlag = viewModel.getAutoSaveFlag();

        String msgDirPath = getContext().getExternalFilesDir(null).toString() + "/Conversation";
        msgDir = new File(msgDirPath);
        if(!msgDir.exists()) {
            msgDir.mkdir();
            Log.d("Conversation", "msgDir 생성 : " + msgDirPath);
        }

        // msgFileName 설정
        msgFileInit();

        //초기 참여자 목록 설정
        userListInit();

        //채팅
        chatRecyclerInit();
        client.publish(client.settingData.getTopic() + "/login", client.settingData.getUserName());


        /* ----------------------------------    OnClickListener 함수        ---------------------------------------------------------------------------------*/

        binding.btnMic.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:

//                    synchronized (client.getPlayThreadList()) {
//                        for(Iterator<PlayThread> itr = client.getPlayThreadList().iterator(); itr.hasNext();) {
//                            PlayThread playThread = itr.next();
//                            Toast.makeText(getActivity(), playThread.getUserName(), Toast.LENGTH_SHORT).show();
//                        }
//                    }



                    /* 오디오 데이터 전달 속도 측정 */
                    /*client.setAudioStartTime(SystemClock.elapsedRealtime());
                    Log.d("Performance", "audioStartTime:"+client.getAudioStartTime());*/

                    client.publish(client.getTopic_speakMark(), client.getUserName() + "&" +"start");
                    binding.btnMic.setImageResource(R.drawable.mic_on);
                    callingViewModel.touchMic();
                    break;
                case MotionEvent.ACTION_UP:
                    client.publish(client.getTopic_speakMark(), client.getUserName() + "&" +"stop");
                    binding.btnMic.setImageResource(R.drawable.mic_off);
                    callingViewModel.touchMic();
                    break;

                default:
                    break;
            }
            return false;
        });

        /* 대화 내용 저장 */
        binding.save.setOnClickListener(v -> {
            conversationSave(0);
        });

        //나가기 버튼 리스너
        binding.exit.setOnClickListener(v -> {
            AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());

            View layout = inflater.inflate(R.layout.dialog_exit, null);
            dialog.setView(layout);

            CheckBox cbSave = (CheckBox) layout.findViewById(R.id.cbSave);
            if(autoSaveFlag) {
                cbSave.setChecked(true);
            }
            cbSave.setOnClickListener(new CheckBox.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(cbSave.isChecked()) {
                        autoSaveFlag = true;
                    } else {
                        autoSaveFlag = false;
                    }
                }
            });

            dialog.setNegativeButton("취소", null);

            dialog.setPositiveButton("나가기", new DialogInterface.OnClickListener() {
                @SneakyThrows
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    /* 대화 내용 저장 */
                    if (autoSaveFlag) {
//                        Log.d("Conversation", "대화 내용 저장");
                        conversationSave(0);
                    }

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
                    observeTextFlag = false;
                    Navigation.findNavController(binding.getRoot()).navigate(R.id.action_chatRoomFragment_to_homeFragment);
                }
            });

            dialog.create().show();
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

                if(autoSaveFlag) {
                    conversationSave(1);
                }
            }
        });

        viewModel.logoutUser.observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String s) {
                if(observeTextFlag) {
                    String user = viewModel.getLogoutUser();
                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                    Date time = new Date();
                    String timeString = format.format(time);
                    dataList.add(new MessageItem(user + "님이 퇴장했습니다.", null, null, timeString, Code.ViewType.CENTER_CONTENT));
                    recyvlerv.setAdapter(new ChatMessageAdapter(dataList));
                    recyvlerv.scrollToPosition(dataList.size() - 1);

                    if(autoSaveFlag) {
                        conversationSave(1);
                    }
                }
            }
        });

        viewModel.userSpeakState.observe(getViewLifecycleOwner(), new Observer<UserSpeakState>() {
            @Override
            public void onChanged(UserSpeakState userSpeakState) {
                String speakUser =  userSpeakState.speakUser;
                String state = userSpeakState.speakState;
                viewModel.editUserSpeakState(speakUser, state);
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
                    updateUserListEmotion(sendUser, image);
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
    private void updateUserListEmotion(String username, String image){
        viewModel.updateUserListEmotion(username, image);
    }

    //참여자 목록 UPDATE함수
    private void userListUpdate(){
        layoutManager = new LinearLayoutManager(getActivity(),  LinearLayoutManager.HORIZONTAL, false);
        listView.setLayoutManager(layoutManager);
        adapter = new RecyclerViewAdapter(getActivity(), viewModel);
        listView.setAdapter(adapter);

        UserListDecoration decoration = new UserListDecoration();
        listView.addItemDecoration(decoration);
    }

    //채팅창 recyclerView 생성 함수
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

        if(autoSaveFlag) {
            conversationSave(1);
        }
    }

    // 대화내용을 저장할 파일명 지정 함수
    public void msgFileInit() {
        /* 파일 이름 : 날짜시간_방이름.txt 또는 방이름_날짜시간.txt */
        SimpleDateFormat dateFormat = new SimpleDateFormat("YYYYMMddHHmm");
        Date currentTime = new Date();
        String date = dateFormat.format(currentTime);

        msgFileName = "/" + callingViewModel.getTopic() + "_" + date + ".txt";
    }

    /* 대화 내용 저장 함수 (saveFlag 0 : 전체 내용 저장, 1: 채팅 하나씩 저장*/
    public void conversationSave(int saveFlag) {
        try {
            FileWriter fileWriter;
            BufferedWriter bufferedWriter;

            msgFile = new File(msgDir + msgFileName);
            if(!msgFile.exists()) {
                msgFile.createNewFile();
            }

//            Log.d("Conversation", "msgFilename : " + msgFileName + "생성");

            /* 파일에 대화 내용 쓰기 */
            if(saveFlag == 0) {
                fileWriter = new FileWriter(msgFile, false);
                bufferedWriter = new BufferedWriter(fileWriter);

                for (MessageItem msgItem : dataList) {
                    // [보낸사람] [시간] [감정] 대화 내용
                    bufferedWriter.append("[" + msgItem.getName() + "] ");
                    bufferedWriter.append("[" + msgItem.getTime() + "] ");
                    bufferedWriter.append("[" + msgItem.getImg() + "] ");
                    bufferedWriter.append(msgItem.getContent());
                    bufferedWriter.newLine();

//                    Log.d("Conversation", "msg0 : " + "[" + msgItem.getName() + "] " + "[" + msgItem.getTime() + "] " + "[" + msgItem.getImg() + "] " + msgItem.getContent());
                }
            }
            else {
                fileWriter = new FileWriter(msgFile, true);
                bufferedWriter = new BufferedWriter(fileWriter);

                MessageItem msgItem = dataList.get(dataList.size()-1);
                // [보낸사람] [시간] [감정] 대화 내용
                bufferedWriter.append("[" + msgItem.getName() + "] ");
                bufferedWriter.append("[" + msgItem.getTime() + "] ");
                bufferedWriter.append("[" + msgItem.getImg() + "] ");
                bufferedWriter.append(msgItem.getContent());
                bufferedWriter.newLine();

//                Log.d("Conversation", "msg1 : " + "[" + msgItem.getName() + "] " + "[" + msgItem.getTime() + "] " + "[" + msgItem.getImg() + "] " + msgItem.getContent());
            }

            bufferedWriter.close();
            fileWriter.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        //Toast.makeText(getActivity(), "onDestroyView", Toast.LENGTH_SHORT).show();

    }

    @SneakyThrows
    @Override
    public void onDestroy() {
        super.onDestroy();
        //Toast.makeText(getActivity(), "onDestroy", Toast.LENGTH_SHORT).show();
        binding = null;
    }

    private void logout(){
        /* 이전의 출력된 텍스트 지우기 */
        dataList.clear();

        client.getConnectOptions().setAutomaticReconnect(false);

        /* Audio 관련 처리 */
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
        synchronized (client.getPlayThreadList()) {
            for(Iterator<PlayThread> itr = client.getPlayThreadList().iterator(); itr.hasNext();) {
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
        }
        /*for(Iterator<PlayThread> itr = client.getPlayThreadList().iterator(); itr.hasNext();){
            PlayThread playThread = itr.next();
            if(callingViewModel.getPlayFlag()) {
                playThread.setPlayFlag(false);
            }
            playThread.stopPlaying();
            synchronized (playThread.getAudioQueue()) {
                playThread.getAudioQueue().clear();
            }
            playThread.interrupt();
        }*/
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
        client.disconnect1();
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