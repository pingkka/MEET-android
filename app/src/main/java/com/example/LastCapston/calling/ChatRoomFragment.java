package com.example.LastCapston.calling;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

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

    //????????? ?????????
    private RecyclerView listView;
    private RecyclerViewAdapter adapter;
    private LinearLayoutManager layoutManager;

    //?????? ?????????
    private ArrayList<MessageItem> dataList;
    private ChatMessageAdapter chatMessageAdapter;
    private RecyclerView recyvlerv;

    private boolean observeTextFlag = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentChatRoomBinding.inflate(inflater, container, false);
        callingViewModel = new ViewModelProvider(this).get(CallingViewModel.class);

        binding.roomName.setText(callingViewModel.getTopic());

        //?????? ????????? ?????? ??????
        userListInit();

        //??????
        chatRecyclerInit();
        client.publish(client.settingData.getTopic() + "/login", client.settingData.getUserName());


        /* ----------------------------------    OnClickListener ??????        ---------------------------------------------------------------------------------*/

        binding.btnMic.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:

//                    synchronized (client.getPlayThreadList()) {
//                        for(Iterator<PlayThread> itr = client.getPlayThreadList().iterator(); itr.hasNext();) {
//                            PlayThread playThread = itr.next();
//                            Toast.makeText(getActivity(), playThread.getUserName(), Toast.LENGTH_SHORT).show();
//                        }
//                    }




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

        //????????? ?????? ?????????
        binding.exit.setOnClickListener(v -> {
            AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());

            dialog.setTitle("??? ?????????")
                    .setMessage("?????? ??????????????????????")
                    .setNegativeButton("??????", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    })
                    .setPositiveButton("?????????", new DialogInterface.OnClickListener() {
                        @SneakyThrows
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            String roomID = client.settingData.getTopic();
                            String user = client.settingData.getUserName();
                            System.out.println(viewModel.getUserList().toString());
                            client.publish(roomID+"/logout", user);
                            ArrayList<String> userList = viewModel.getUserList();
                            logout();

                            /* firebase ??????????????? ??????, mqtt ?????? ?????????*/
                            databaseLogout(userList, roomID, user);

                            /* MQTTClient ?????? ?????? */

                            client.getParticipantsList().clear();
                            client.getConnectOptions().setAutomaticReconnect(false);

                            /* view?????? ????????? */
                            //MQTTSettingData ?????????
                            viewModel.initMQTTSettingData();
                            //mainViewmodel ?????????
                            viewModel.mainViewMoedlInit();
                            observeTextFlag = false;
                            Navigation.findNavController(binding.getRoot()).navigate(R.id.action_chatRoomFragment_to_homeFragment);


                        }
                    })
                    .show();
        });

        /* -------------------------------------    observe ??????        ---------------------------------------------------------------------------------*/
        //????????? ?????? ??????
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
                dataList.add(new MessageItem(user + "?????? ??????????????????.", null,null, timeString, Code.ViewType.CENTER_CONTENT));
                recyvlerv.setAdapter(new ChatMessageAdapter(dataList));
                recyvlerv.scrollToPosition(dataList.size()-1);
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
                    dataList.add(new MessageItem(user + "?????? ??????????????????.", null, null, timeString, Code.ViewType.CENTER_CONTENT));
                    recyvlerv.setAdapter(new ChatMessageAdapter(dataList));
                    recyvlerv.scrollToPosition(dataList.size() - 1);
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

    /* --------------------------------------------   ??? ??? ??????        ---------------------------------------------------------------------------------*/

    //????????? ?????? ?????? ??????
    public void userListInit(){
        listView = binding.userListView;
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity(),  LinearLayoutManager.HORIZONTAL, false);
        listView.setLayoutManager(layoutManager);

        adapter = new RecyclerViewAdapter(getActivity(), viewModel);
        listView.setAdapter(adapter);

        UserListDecoration decoration = new UserListDecoration();
        listView.addItemDecoration(decoration);
    }

    //????????? ?????? UPDATE??????
    private void updateUserListEmotion(String username, String image){
        viewModel.updateUserListEmotion(username, image);
    }

    //????????? ?????? UPDATE??????
    private void userListUpdate(){
        layoutManager = new LinearLayoutManager(getActivity(),  LinearLayoutManager.HORIZONTAL, false);
        listView.setLayoutManager(layoutManager);
        adapter = new RecyclerViewAdapter(getActivity(), viewModel);
        listView.setAdapter(adapter);

        UserListDecoration decoration = new UserListDecoration();
        listView.addItemDecoration(decoration);
    }

    //????????? recyclerView ?????? ??????
    private void chatRecyclerInit(){
        dataList = new ArrayList();
        recyvlerv = binding.recyvlerv;
        LinearLayoutManager manager = new LinearLayoutManager(getActivity(), RecyclerView.VERTICAL, false);
        recyvlerv.setLayoutManager(manager);
        recyvlerv.setAdapter(new ChatMessageAdapter(dataList));
    }


    // ????????? text ?????? ??????
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
        /* ????????? ????????? ????????? ????????? */
        dataList.clear();

        client.getConnectOptions().setAutomaticReconnect(false);

        /* Audio ?????? ?????? */
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

        /* PlayThreadList??? ?????? PlayThread interrupt ??? PlayThreadList ?????????*/
        //java.util.ConcurrentModificationException ?????? ??? ?????? ??????
        //index??? ??????????????? ????????? ????????? ???????????? ??????
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

        //?????? ?????? ??????

        /* MQTTClient ?????? ?????? */
        client.getParticipantsList().clear();
        client.disconnect1();
        client.getConnectOptions().setAutomaticReconnect(false);
    }

    /* ??????????????? firebase?????? ??????????????? ?????? */
    public void databaseLogout(ArrayList<String> userList, String roomID, String user) throws Exception {
        System.out.println(userList.size());

        DocumentReference docRef = db.collection("rooms").document(roomID);
        if (userList.size() == 1) {
            docRef
                    .delete()
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.w(TAG, "?????? ??????");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w(TAG, "???????????? ??? ?????? ??????", e);
                        }
                    });
        } else {
            // Remove the 'capital' field from the document
            Map<String, Object> updates = new HashMap<>();
            updates.put("participants", FieldValue.arrayRemove(user));
            docRef.update(updates).addOnCompleteListener(new OnCompleteListener<Void>() {

                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    Log.w(TAG, "?????? ?????? ?????? ??????");
                }
            });
        }
    }
}