package com.microblocklabs.mpc.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.microblocklabs.mpc.R;
import com.microblocklabs.mpc.model.PhraseData;

import java.util.ArrayList;

public class PhraseRecyclerViewAdapter extends RecyclerView.Adapter<PhraseRecyclerViewAdapter.RecyclerViewHolder> {

    private ArrayList<PhraseData> phraseDataArrayList;
    private Context mcontext;
    private Boolean isEditablePhrase;

    public PhraseRecyclerViewAdapter(ArrayList<PhraseData> phraseDataArrayList, Context mcontext, Boolean isEditMode) {
        this.phraseDataArrayList = phraseDataArrayList;
        this.mcontext = mcontext;
        this.isEditablePhrase = isEditMode;
    }

    @NonNull
    @Override
    public RecyclerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate Layout
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_phrase, parent, false);
        return new RecyclerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerViewHolder holder, int position) {
        // Set the data to textview and imageview.
        PhraseData phraseData = phraseDataArrayList.get(position);
        holder.phraseNumber.setText(String.valueOf(phraseData.getPhraseNumber()));
        holder.phrase.setText(phraseData.getPhrase());
        if(isEditablePhrase){
            holder.phrase.setEnabled(true);
        }else{
            holder.phrase.setEnabled(false);
        }

    }

    @Override
    public int getItemCount() {
        // this method returns the size of recyclerview
        return phraseDataArrayList.size();
    }

    // View Holder Class to handle Recycler View.
    public class RecyclerViewHolder extends RecyclerView.ViewHolder {

        private final TextView phraseNumber;
        private final EditText phrase;

        public RecyclerViewHolder(@NonNull View itemView) {
            super(itemView);

            phraseNumber = itemView.findViewById(R.id.tv_phrase_number);
            phrase = itemView.findViewById(R.id.et_phrase);
        }
    }
}

