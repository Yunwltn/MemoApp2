package com.yunwltn98.memoapp.adapter;

import static android.provider.Settings.System.getString;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.yunwltn98.memoapp.MainActivity;
import com.yunwltn98.memoapp.R;
import com.yunwltn98.memoapp.UpdateActivity;
import com.yunwltn98.memoapp.api.MemoApi;
import com.yunwltn98.memoapp.api.NetworkClient;
import com.yunwltn98.memoapp.config.Config;
import com.yunwltn98.memoapp.model.Memo;
import com.yunwltn98.memoapp.model.Res;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class MemoAdapter extends RecyclerView.Adapter<MemoAdapter.ViewHolder> {

    Context context;
    ArrayList<Memo> memoArrayList;
    private int deleteIndex;

    public MemoAdapter(Context context, ArrayList<Memo> memoArrayList) {
        this.context = context;
        this.memoArrayList = memoArrayList;
    }

    @NonNull
    @Override
    public MemoAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.memo_row, parent, false);
        return new MemoAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MemoAdapter.ViewHolder holder, int position) {
        Memo memo = memoArrayList.get(position);
        holder.txtTitle.setText(memo.getTitle());
        // "datetime": "2023-02-26T00:00:00" > 2023-02-26 00:00,
        String date = memo.getDatetime().replace("T", " ").substring(0,15+1);
        holder.txtDate.setText(date);
        holder.txtContent.setText(memo.getContent());

    }

    @Override
    public int getItemCount() {
        return memoArrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView txtTitle;
        TextView txtDate;
        TextView txtContent;
        ImageView imgDelete;
        CardView cardView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            txtTitle = itemView.findViewById(R.id.txtTitle);
            txtDate = itemView.findViewById(R.id.txtDate);
            txtContent = itemView.findViewById(R.id.txtContent);
            imgDelete = itemView.findViewById(R.id.imgDelete);
            cardView = itemView.findViewById(R.id.cardView);

            // 메모 수정
            cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int index = getAdapterPosition();
                    Memo memo = memoArrayList.get(index);

                    Intent intent = new Intent(context, UpdateActivity.class);
                    intent.putExtra("memo", memo);
                    context.startActivity(intent);
                }
            });

            // 메모 삭제
            imgDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    deleteIndex = getAdapterPosition();

                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle("메모 삭제");
                    builder.setMessage("정말 삭제하시겠습니까");
                    builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                            int index = getAdapterPosition();
                            ((MainActivity)context).deleteMemo(index);
                        }
                    });
                    builder.setNegativeButton("NO", null);
                    builder.show();
                }
            });
        }
    }
}

