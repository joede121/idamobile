package com.example.idamusic_mobile;

import android.graphics.drawable.BitmapDrawable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.idamusic_mobile.DummyContent.DummyItem;
import com.example.idamusic_mobile.ItemFragment.OnListFragmentInteractionListener;

import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link DummyItem} and makes a call to the
 * specified {@link OnListFragmentInteractionListener}.
 * TODO: Replace the implementation with code for your data type.
 */
public class PlayerSelectActivityAdapter extends RecyclerView.Adapter<PlayerSelectActivityAdapter.ViewHolder> {

    private final List<String> mPlayers;
    private final selectPlayerListener mListener;
    private final String mActPlayer;

    public PlayerSelectActivityAdapter(List<String> players, selectPlayerListener listener, String actplayer) {
        mPlayers = players;
        mListener = listener;
        mActPlayer = actplayer;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_item_players, parent, false);
        return new ViewHolder(view);
    }


    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {

        holder.mPlayer = mPlayers.get(position);
        holder.mTextPlayer.setText(holder.mPlayer);
        //holder.mIdSong.setText(position + 1 +"");
        //holder.mArtistSong.setText(holder.mSong.getArtist());
        //holder.mIdView.setText(mValues.get(position).playable.artist);
        //holder.mContentView.setText(mValues.get(position).playable.name);
        // BitmapDrawable ob = new BitmapDrawable(mValues.get(position).playable.image);
        //    holder.mWebView.setImageDrawable(ob);
        // holder.mView.setBackground(ob);
        BitmapDrawable ob;
        switch(holder.mPlayer) {
            case SpotifyPlayer.PIEPSER_DEVICE_NAME:
                holder.mImageViewPlayer.setImageResource(R.drawable.ic_einhorn);
                break;
            case SpotifyPlayer.THIS_DEVICE_NAME:
                holder.mImageViewPlayer.setImageResource(R.drawable.ic_smartphone_24px);
                break;
            case OfflinePlayerSqueeze.PLAYER_BATHROOM:
                holder.mImageViewPlayer.setImageResource(R.drawable.ic_bathtub_24px);
                break;
            case OfflinePlayerSqueeze.PLAYER_LIVINGROOM:
                holder.mImageViewPlayer.setImageResource(R.drawable.ic_tv_24px);
                break;
            case OfflinePlayerSqueeze.PLAYER_BEDROOM:
                holder.mImageViewPlayer.setImageResource(R.drawable.ic_king_bed_24px);
                break;
        }

        if(holder.mPlayer.equals(mActPlayer)){
            holder.mView.setBackgroundResource(R.drawable.radius_bg_4_highlight);
        }
        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("PlayerSelect", "OnClick");
                v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onPlayerSelect(holder.mPlayer);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mPlayers.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mTextPlayer;
        public final ImageView mImageViewPlayer;
        public String mPlayer;


        //        public final ImageView mWebVie
        public ViewHolder(View view) {
            super(view);
            mView = view;
            mImageViewPlayer = (ImageView) view.findViewById(R.id.imageViewPlayersPlayer);
            mTextPlayer = (TextView) view.findViewById(R.id.textPlayersName);

        }

        @Override
        public String toString() {
            return super.toString() + " '" + mPlayer + "'";
        }
    }

    public interface selectPlayerListener{
        public void onPlayerSelect(String player);
    }
}
