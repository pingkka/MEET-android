package com.example.LastCapston.loadFile;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager.LayoutParams;

import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.LastCapston.R;
import com.example.LastCapston.adapter.ChatMessageAdapter;
import com.example.LastCapston.data.Code;
import com.example.LastCapston.data.MessageItem;
import com.example.LastCapston.databinding.FragmentLoadChatBinding;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LoadChatFragment extends Fragment {

    private FragmentLoadChatBinding binding = null;

    //채팅 리스트
    private ArrayList<MessageItem> dataList;
    private RecyclerView recyvlerv;

    private String topic;

    private File msgDir;
    private File msgFile;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentLoadChatBinding.inflate(inflater, container, false);

        //채팅
        chatRecyclerInit();

        String msgDirPath = getContext().getExternalFilesDir(null).toString() + "/Conversation";
        msgDir = new File(msgDirPath);

        // 파일 선택
        showFileSelectDialog();

        /* ----------------------------------    OnClickListener 함수        ---------------------------------------------------------------------------------*/
        //나가기 버튼 리스너
        binding.exit.setOnClickListener(v -> {
            Navigation.findNavController(binding.getRoot()).navigate(R.id.action_loadChatFragment_to_homeFragment);
        });

        //파일 선택하기 버튼 리스너
        binding.load.setOnClickListener(v-> {
            showFileSelectDialog();
        });

        return binding.getRoot();
    }

    /* 해당 폴더 안 파일 목록 불러오기 함수 */
    private List<String> getFileList() {
        File[] files = msgDir.listFiles();

        List<String>fileNameList = new ArrayList<>();

        for(int i=0; i<files.length; i++) {
            fileNameList.add(files[i].getName());
        }

        return fileNameList;
    }

    /* 파일 선택 다이얼로그 보여주기 함수 */
    private void showFileSelectDialog() {
        List<String> fileNameList = getFileList();
        String[] fileNames = fileNameList.toArray(new String[fileNameList.size()]);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("파일을 선택하세요");

        builder.setSingleChoiceItems(fileNames, -1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String msgFileName = fileNames[which];
                msgFile = new File(msgDir + "/" + msgFileName);

                topic = msgFileName.split("_")[0];
            }
        });

        builder.setNegativeButton("취소", null);

        builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                binding.roomName.setText(topic);
                Log.d("Conversation", "topic : " + topic);

                conversationLoad();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();

        LayoutParams layoutParams = dialog.getWindow().getAttributes();
        if(fileNameList.size() > 6) {
            layoutParams.height = 1100;
        }
        dialog.getWindow().setAttributes(layoutParams);
    }

    //채팅창 recyclerView 생성 함수
    private void chatRecyclerInit() {
        dataList = new ArrayList();
        recyvlerv = binding.recyvlerv;
        LinearLayoutManager manager = new LinearLayoutManager(getActivity(), RecyclerView.VERTICAL, false);
        recyvlerv.setLayoutManager(manager);
        recyvlerv.setAdapter(new ChatMessageAdapter(dataList));
    }

    /* 대화 내용 불러오기 함수 */
    public void conversationLoad() {
        try {
            FileReader fileReader = new FileReader(msgFile);
            BufferedReader bufferedReader = new BufferedReader(fileReader);

            /* 대화 내용 불러오기 */
            dataList = new ArrayList();
            String line = "";
            while((line = bufferedReader.readLine()) != null) {
                Log.d("Conversation", "line : " + line);

                String content = line.substring(line.lastIndexOf("]") +1).trim();
//                Log.d("Conversation", "content : " + content);

                // []로 name, time, img, content 추출
                Pattern pattern = Pattern.compile("(\\[)(.*?)(\\])");
                Matcher matcher = pattern.matcher(line);

                List<String> strList = new ArrayList<>();
                while(matcher.find()) {
                    strList.add(matcher.group(2).trim());
                }

                String name = strList.get(0);
                String time = strList.get(1);
                String img = strList.get(2);
//                Log.d("Conversation", "name : " + name);
//                Log.d("Conversation", "time : " + time);
//                Log.d("Conversation", "img : " + img);

                int viewType;
                if(name.equals("null")) {
                    if(img.equals("")) {
                        viewType = Code.ViewType.CENTER_CONTENT;
                        img = null;
                    }
                    else {
                        viewType = Code.ViewType.RIGHT_CONTENT;
                    }

                    name = null;
                }
                else {
                    viewType = Code.ViewType.LEFT_CONTENT;
                }
//                Log.d("Conversation", "viewType : " + viewType);

                dataList.add(new MessageItem(content, name, img, time, viewType));
            }

//            Log.d("Conversation", "dataList Size : " + dataList.size());
            recyvlerv.setAdapter(new ChatMessageAdapter(dataList));

            bufferedReader.close();
            fileReader.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
