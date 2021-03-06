package osfma.mcm.fhooe.at.livetickerprivate.ui.game.adapter;

import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;
import com.firebase.client.ValueEventListener;
import com.firebase.ui.FirebaseListAdapter;

import osfma.mcm.fhooe.at.livetickerprivate.R;
import osfma.mcm.fhooe.at.livetickerprivate.model.GameEvent;
import osfma.mcm.fhooe.at.livetickerprivate.model.User;
import osfma.mcm.fhooe.at.livetickerprivate.utils.Constants;
import osfma.mcm.fhooe.at.livetickerprivate.utils.Helper;

/**
 * Created by Tob0t on 24.02.2016.
 */
public class GameDetailListAdapter extends FirebaseListAdapter<GameEvent> {
    private static final String LOG_TAG = GameDetailListAdapter.class.getSimpleName();
    protected int[] mLayout;

    public GameDetailListAdapter(Activity activity, Class<GameEvent> modelClass, int[] modelLayout, Query ref) {
        super(activity, modelClass, modelLayout[0], ref);
        this.mActivity = activity;
        this.mLayout = modelLayout;

    }

    @Override
    protected void populateView(View view, GameEvent gameEvent, int i) {
        TextView author = (TextView) view.findViewById(R.id.text_view_owner);
        TextView message = (TextView) view.findViewById(R.id.text_view_message);
        TextView timestamp = (TextView) view.findViewById(R.id.text_view_timestamp);

        message.setText(gameEvent.getMessage());
        timestamp.setText(Helper.TIME_FORMATTER.format(gameEvent.getTimestampSentLong()));


        switch (gameEvent.getType()) {
            case SCORE:
                setOwnerName(author, gameEvent.getUserId());
                showScoreEvent(view, gameEvent);
                break;
            case CHAT:
                setOwnerName(author, gameEvent.getUserId());
                showChatEvent(view, gameEvent);
                break;
            case INFO:
                showInfoEvent(view, gameEvent);
        }
    }

    private void setOwnerName(final TextView textViewOwner, String userId) {
        Firebase userRef = new Firebase(Constants.FIREBASE_URL_USERS).child(userId);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                if (user != null) {
                    textViewOwner.setText(user.getName());
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                Log.e(LOG_TAG, mActivity.getString(R.string.log_error_the_read_failed)
                        + firebaseError.getMessage());
            }
        });
    }

    private void showScoreEvent(View view, GameEvent gameEvent) {
        TextView info = (TextView) view.findViewById(R.id.text_view_info);
        info.setText(gameEvent.getInfo());
    }

    private void showChatEvent(View view, GameEvent gameEvent) {

    }

    private void showInfoEvent(View view, GameEvent gameEvent) {
    }

    @Override
    public int getItemViewType(int position) {
        GameEvent model = this.getItem(position);
        return model.getType().ordinal();
    }

    // Inflate different Layout depending on ItemViewType
    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {
        super.getView(position, view, viewGroup);
        if (view == null) {
            view = mActivity.getLayoutInflater().inflate(mLayout[getItemViewType(position)], viewGroup, false);
        }

        GameEvent model = getItem(position);

        // Call out to subclass to marshall this model into the provided view
        populateView(view, model, position);
        return view;
    }

    @Override
    public int getViewTypeCount() {
        return mLayout.length;
    }
}
