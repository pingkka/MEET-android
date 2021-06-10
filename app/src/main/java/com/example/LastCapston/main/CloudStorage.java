package com.example.LastCapston.main;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.navigation.Navigation;

import com.example.LastCapston.R;
import com.example.LastCapston.adapter.ChatMessageAdapter;
import com.example.LastCapston.data.MQTTSettingData;
import com.example.LastCapston.databinding.FragmentCreateRoomBinding;
import com.example.LastCapston.home.CreateRoomFragment;
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

    public CloudStorage(Context context, MainViewModel viewModel) {
        this.context = context;
        this.viewModel = viewModel;

    }

    /* 방 생성 버튼 클릭시 실행되는 코드(입장 하기전 이미 방이 있는지, 아이디,비밀번호가 올바른지 확인)  */
//    public void checkRoomBeforeCreate(String roomID, String roomPW, String user) {
//
//        //아이디, 비밀번호, 방이름이 공백인지 확인한다
//        if (!checkLogin(roomID, roomPW, user)) {
//            return;
//        }
//
//        //firebase에서 동일한 이름의 roomID가 있으면 토스트메세지로 알려주고 없으면 생성해주는 코드
//        DocumentReference docRef = db.collection("rooms").document(roomID);
//        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
//            @Override
//            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
//                if (task.isSuccessful()) {
//                    DocumentSnapshot document = task.getResult();
//                    if (document.exists()) {
//                        Toast.makeText(context, "이미 동일한 이름의 방이 있습니다. ", Toast.LENGTH_SHORT).show();
//                    } else {
//                        //같은 이름의 방이 없으면 방 생성
//                        storeDB(roomID, roomPW, user);
//
//                    }
//                } else {
//                    Log.d("tag", "get failed with ", task.getException());
//                }
//            }
//        });
//    }
//
//    /* firebase에 방 정보 및 참여자 저장 함수  */
//    public void storeDB(String roomID, String roomPW, String participant) {
//
//        CollectionReference rooms = db.collection("rooms");
//        Map<String, Object> user = new HashMap<>();
//        user.put("roomeID", roomID);
//        user.put("roomPW", roomPW);
//        user.put("participants", Arrays.asList(participant));
//        rooms.document(roomID).set(user);
//        Toast.makeText(context, "방을 생성했습니다.", Toast.LENGTH_SHORT).show();
//
//        //mqtt sub
//        client.init(settingData.getTopic(), settingData.getUserName(), settingData.getIp(), settingData.getPort(),  viewModel);
//        client.subscribeAll();
//        viewModel.setEnterFlag(true);
//        client.publish(settingData.getTopic() + "/login", settingData.getUserName());
//
//    }
//
//    //EnterRoomFragment 에서 사용
//    // roomID,PW, 참여자 있는지 확인하고 조건에 만족하면 추가
//    /* 방 입장 버튼 클릭시 실행되는 코드(입장 하기전 이미 방이 있는지 검사, 아이디,비밀번호가 올바른지 확인)  */
//    public void checkRoomBeforeEnter(String roomID, String roomPW, String user) {
//        checkLogin(roomID, roomPW, user);
//        DocumentReference docRef = db.collection("rooms").document(roomID);
//        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
//            @Override
//            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
//                if (task.isSuccessful()) {
//                    DocumentSnapshot document = task.getResult();
//                    ArrayList<String> getparticipants = new ArrayList<String>();
//                    if (document.exists()) {
//                        getparticipants = (ArrayList<String>) document.getData().get("participants");
//                    }
//                    if (!document.exists()) {
//                        Toast.makeText(context, "room 없음 ", Toast.LENGTH_SHORT).show();
//                    } else if (!document.getData().get("roomPW").toString().equals(roomPW)) {
//                        Toast.makeText(context, "비번 틀림", Toast.LENGTH_SHORT).show();
//                    } else if (getparticipants.contains(user) == true) {
//                        Toast.makeText(context, "같은 이름 있음", Toast.LENGTH_SHORT).show();
//                    } else {
//                        Toast.makeText(context, "저장 성공", Toast.LENGTH_SHORT).show();
//                        DocumentReference Ref = db.collection("rooms").document(roomID);
//                        Ref.update("participants", FieldValue.arrayUnion(user));
//                        try {
//                            //mqtt sub
//                            client.init(settingData.getTopic(), settingData.getUserName(), settingData.getIp(), settingData.getPort(), viewModel);
//                            client.subscribeAll();
//                            viewModel.setEnterFlag(true);
//                            client.publish(settingData.getTopic() + "/login", settingData.getUserName());
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                        }
//
//
//                    }
//                } else {
//                    Log.d(TAG, "get failed with ", task.getException());
//                }
//            }
//        });
//
//
//    }
//
//    /* 로그인시 공백을 검사하는 함수 */
//    public boolean checkLogin(String roomID, String roomPW, String user) {
//        if (roomID.equals("")) {
//            Toast.makeText(context, "roomID를 작성해주세요. ", Toast.LENGTH_SHORT).show();
//            return false;
//        } else if (roomPW.equals("")) {
//            Toast.makeText(context, "roomPW를 작성해주세요.", Toast.LENGTH_SHORT).show();
//            return false;
//        } else if (user.equals("")) {
//            Toast.makeText(context, "사용자ID를 작성해주세요.", Toast.LENGTH_SHORT).show();
//            return false;
//        }
//        return true;
//    }

//    /* 로그아웃시 firebase에서 삭제해주는 함수 */
//    public void logout(String roomID, String user) throws Exception {
//        ArrayList<String> userList = viewModel.getUserList();
//        //participantsList = mqttAndroidClient.getParticipantsList();
//        DocumentReference docRef = db.collection("rooms").document(roomID);
//        if (userList.size() == 1) {
//            docRef
//                    .delete()
//                    .addOnSuccessListener(new OnSuccessListener<Void>() {
//                        @Override
//                        public void onSuccess(Void aVoid) {
//                            Log.w(TAG, "사람 없음");
//
//                            //view모델 초기화
//                            viewModel.userInit();
//
//                            /* MQTTClient 연결 해제 */
//                            client.disconnect();
//                            client.getParticipantsList().clear();
//                            client.getConnectOptions().setAutomaticReconnect(false);
//
//                        }
//                    })
//                    .addOnFailureListener(new OnFailureListener() {
//                        @Override
//                        public void onFailure(@NonNull Exception e) {
//                            Log.w(TAG, "로그아웃 방 삭제 실패", e);
//                        }
//
//                    });
//        } else {
//            // Remove the 'capital' field from the document
//            Map<String, Object> updates = new HashMap<>();
//            updates.put("participants", FieldValue.arrayRemove(user));
//            docRef.update(updates).addOnCompleteListener(new OnCompleteListener<Void>() {
//
//                @Override
//                public void onComplete(@NonNull Task<Void> task) {
//                    Log.w(TAG, "아직 방에 사람 있음");
//                    //나갔다고 알리기
//                    client.publish(roomID+"/logout", user);
//
//                    //view모델 초기화
//                    viewModel.userInit();
//
//                    /* MQTTClient 연결 해제 */
//                    client.getParticipantsList().clear();
//                    client.disconnect();
//                    client.getConnectOptions().setAutomaticReconnect(false);
//
//
//                }
//            });
//        }
//
//    }
}
