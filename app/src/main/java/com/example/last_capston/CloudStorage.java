package com.example.last_capston;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;
import androidx.annotation.NonNull;

import com.example.last_capston.calling.CallingViewModel;
import com.example.last_capston.main.MQTTClient;
import com.example.last_capston.main.MQTTSettingData;
import com.example.last_capston.main.MainViewModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static android.content.ContentValues.TAG;

public class CloudStorage {
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private MQTTClient client = MQTTClient.getInstance();
    private MQTTSettingData settingData = MQTTSettingData.getInstance();
    private Context context;
    private static MainViewModel viewModel = MainViewModel.getInstance();
    private static CallingViewModel callingViewModel;

    public CloudStorage(Context context, MainViewModel viewModel) {
        this.context = context;
        this.viewModel = viewModel;

    }

    public void checkRoomBeforeCreate(String roomID, String roomPW, String user) {
        //빈칸 검사
        if (!checkLogin(roomID, roomPW, user)) {
            return;
        }
        //
        DocumentReference docRef = db.collection("rooms").document(roomID);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Toast.makeText(context, "이미 있음 ", Toast.LENGTH_SHORT).show();
                    } else {
                        //같은 이름의 방이 없으면 방 생성
                        storeDB(roomID, roomPW, user);
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
        Toast.makeText(context, "방 생성", Toast.LENGTH_SHORT).show();

        //mqtt sub
        client.init(settingData.getTopic(), settingData.getUserName(), settingData.getIp(), settingData.getPort(), callingViewModel, viewModel);
        client.subscribeAll();
        try {
            client.setParticipantsList(settingData.getUserName());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //EnterRoomFragment 에서 사용
    // roomID,PW, 참여자 있는지 확인하고 조건에 만족하면 추가
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
                        Toast.makeText(context, "room 없음 ", Toast.LENGTH_SHORT).show();
                    } else if (!document.getData().get("roomPW").toString().equals(roomPW)) {
                        Toast.makeText(context, "비번 틀림", Toast.LENGTH_SHORT).show();
                    } else if (getparticipants.contains(user) == true) {
                        Toast.makeText(context, "같은 이름 있음", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(context, "저장 성공", Toast.LENGTH_SHORT).show();
                        DocumentReference Ref = db.collection("rooms").document(roomID);
                        Ref.update("participants", FieldValue.arrayUnion(user));
                        try {
                            //mqtt sub
                            client.init(settingData.getTopic(), settingData.getUserName(), settingData.getIp(), settingData.getPort(), callingViewModel,viewModel);
                            client.subscribeAll();
                            client.publish(settingData.getTopic() + "/login", settingData.getUserName());

                            viewModel.setEnterFlag(true);


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

    public boolean checkLogin(String roomID, String roomPW, String user) {
        if (roomID.equals("")) {
            Toast.makeText(context, "roomID를 작성해주세요. ", Toast.LENGTH_SHORT).show();
            return false;
        } else if (roomPW.equals("")) {
            Toast.makeText(context, "roomPW를 작성해주세요.", Toast.LENGTH_SHORT).show();
            return false;
        } else if (user.equals("")) {
            Toast.makeText(context, "사용자ID를 작성해주세요.", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    public void logout(String roomID, String user) throws Exception {
        ArrayList<String> userList = viewModel.getUserList();
        //participantsList = mqttAndroidClient.getParticipantsList();
        DocumentReference docRef = db.collection("rooms").document(roomID);
        if (userList.size() == 1) {
            docRef
                    .delete()
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.w(TAG, "사람 없음");

                            //view모델 초기화
                            viewModel.userInit();

                            /* MQTTClient 연결 해제 */
                            client.disconnect();
                            client.getParticipantsList().clear();
                            client.getConnectOptions().setAutomaticReconnect(false);


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
                    //나갔다고 알리기
                    client.publish(roomID+"/logout", user);

                    //view모델 초기화
                    viewModel.userInit();

                    /* MQTTClient 연결 해제 */
                    client.disconnect();
                    client.getParticipantsList().clear();
                    client.getConnectOptions().setAutomaticReconnect(false);


                }
            });
        }

    }
}
