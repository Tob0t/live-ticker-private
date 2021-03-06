package osfma.mcm.fhooe.at.livetickerprivate.ui.game.gameDetail;


import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TableRow;
import android.widget.TextView;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.util.ArrayList;

import osfma.mcm.fhooe.at.livetickerprivate.R;
import osfma.mcm.fhooe.at.livetickerprivate.model.Game;
import osfma.mcm.fhooe.at.livetickerprivate.model.GameEvent;
import osfma.mcm.fhooe.at.livetickerprivate.model.GameSet;
import osfma.mcm.fhooe.at.livetickerprivate.ui.MainActivity;
import osfma.mcm.fhooe.at.livetickerprivate.ui.game.adapter.GameDetailListAdapter;
import osfma.mcm.fhooe.at.livetickerprivate.utils.Constants;
import osfma.mcm.fhooe.at.livetickerprivate.utils.Helper;

/**
 * Created by Tob0t on 17.03.2016.
 */
public class GameWatchFragment extends Fragment{
    private static final String LOG_TAG = GameWatchFragment.class.getSimpleName();
    private Firebase mActiveGameRef;
    private Firebase mActiveGameSetsRef;
    private Firebase mGamesEventsRef;
    private Firebase mUserIsTypingRef;
    private GameDetailListAdapter mGameDetailListAdapter;
    private ValueEventListener mActiveGameRefListener;
    private ChildEventListener mActiveGameSetsRefListener;
    private ListView mGameDetailListView;
    private TextView mTeam1Name, mTeam1Points, mTeam2Name, mTeam2Points;
    private ArrayList<TextView> mTeam1PointsSets, mTeam2PointsSets;
    private TextView mTeam1NameTable;
    private TextView mTeam2NameTable;
    private ArrayList<TableRow> mSetTableRows;
    private boolean mCurrentUserIsOwner = false;
    private Constants.GameType mGameType;
    private String mGameId;
    private String mUserId;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_game_watch, container, false);

         /* Get the push ID from the extra passed by MainActivity */
        Intent intent = getActivity().getIntent();
        mGameId = intent.getStringExtra(Constants.KEY_LIST_ID);
        mGameType = (Constants.GameType) intent.getSerializableExtra(Constants.KEY_GAME_TYPE);

        // Get userId from Activity
        mUserId = ((GameDetailActivity)getActivity()).getmUserId();
        /**
         * Create Firebase references
         */
        String gameType = Helper.checkGameType(mGameType);

        mActiveGameRef = new Firebase(gameType).child(mGameId);
        mGamesEventsRef = new Firebase(Constants.FIREBASE_URL_GAMES_EVENTS).child(mGameId);
        mActiveGameSetsRef = new Firebase(gameType).child(mGameId).child(Constants.FIREBASE_LOCATION_GAMES_GAMESETS);
        mUserIsTypingRef = new Firebase(Constants.FIREBASE_URL_USER_TYPING_INDICATOR).child(mUserId);
        mUserIsTypingRef.onDisconnect().removeValue();

        //mActiveGameRef.keepSynced(true);

        initializeScreen(rootView);

        /**
         * Setup the adapter
         */
        int[] listItems = new int[]{
                R.layout.single_game_event_list_item_score,
                R.layout.single_game_event_list_item_message,
                R.layout.single_game_event_list_item_info
        };
        mGameDetailListAdapter = new GameDetailListAdapter(getActivity(), GameEvent.class,
                listItems, mGamesEventsRef);
        /* Create ActiveListItemAdapter and set to listView */
        mGameDetailListView.setAdapter(mGameDetailListAdapter);

        mActiveGameRefListener = mActiveGameRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Game game = dataSnapshot.getValue(Game.class);
                if(game != null) {
                    mTeam1Name.setText(game.getTeam1());
                    mTeam2Name.setText(game.getTeam2());

                    mTeam1NameTable.setText(game.getTeam1());
                    mTeam2NameTable.setText(game.getTeam2());

                    /* Check if the current user is owner */
                    mCurrentUserIsOwner = Helper.checkIfOwner(game, mUserId);

                } else{
                    Helper.showToast(getActivity(), rootView.getResources().getString(R.string.game_deleted));
                    Intent intent = new Intent(getActivity(), MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                }

            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });

        mActiveGameSetsRefListener = mActiveGameSetsRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                // having the right Scores on startup
                updateView(dataSnapshot);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                updateView(dataSnapshot);
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });

        return rootView;
    }

    private void updateView(DataSnapshot dataSnapshot) {
        GameSet gameSet = dataSnapshot.getValue(GameSet.class);
        updateTableScores(dataSnapshot);
        updateTableRows(dataSnapshot);
        if(gameSet.isActive()){
            updateMainScore(gameSet);
        }
    }

    private void updateTableRows(DataSnapshot dataSnapshot) {
        GameSet gameSet = dataSnapshot.getValue(GameSet.class);
        int currentGameSet = Constants.GAMESETS_LIST.indexOf(dataSnapshot.getKey());
        if(gameSet.isActive()) {
            mSetTableRows.get(currentGameSet).setBackgroundColor(Color.YELLOW);
        } else{
            mSetTableRows.get(currentGameSet).setBackgroundColor(Color.TRANSPARENT);
        }
    }

    private void updateMainScore(GameSet gameSet) {
        if(gameSet.isActive()) {
            // write the score in the middle of the active gameSet
            mTeam1Points.setText(String.valueOf(gameSet.getScoreTeam1()));
            mTeam2Points.setText(String.valueOf(gameSet.getScoreTeam2()));
        }
    }

    private void updateTableScores(DataSnapshot dataSnapshot) {
        GameSet gameSet = dataSnapshot.getValue(GameSet.class);
        int currentGameSet = Constants.GAMESETS_LIST.indexOf(dataSnapshot.getKey());
        mTeam1PointsSets.get(currentGameSet).setText(String.valueOf(gameSet.getScoreTeam1()));
        mTeam2PointsSets.get(currentGameSet).setText(String.valueOf(gameSet.getScoreTeam2()));
    }

    private void initializeScreen(View rootView) {

        mGameDetailListView = (ListView) rootView.findViewById(R.id.listView_game_events);

        mTeam1Name = (TextView) rootView.findViewById(R.id.textView_game_detail_team1);
        mTeam1Points = (TextView) rootView.findViewById(R.id.textView_game_detail_team1_points);
        mTeam2Name = (TextView) rootView.findViewById(R.id.textView_game_detail_team2);
        mTeam2Points = (TextView) rootView.findViewById(R.id.textView_game_detail_team2_points);

        mTeam1NameTable = (TextView) rootView.findViewById(R.id.textView_game_detail_table_team1);
        mTeam1PointsSets = new ArrayList<TextView>();
        mTeam1PointsSets.add((TextView) rootView.findViewById(R.id.textView_game_detail_table_team1_set1));
        mTeam1PointsSets.add((TextView) rootView.findViewById(R.id.textView_game_detail_table_team1_set2));
        mTeam1PointsSets.add((TextView) rootView.findViewById(R.id.textView_game_detail_table_team1_set3));

        mTeam2NameTable = (TextView) rootView.findViewById(R.id.textView_game_detail_table_team2);
        mTeam2PointsSets = new ArrayList<TextView>();
        mTeam2PointsSets.add((TextView) rootView.findViewById(R.id.textView_game_detail_table_team2_set1));
        mTeam2PointsSets.add((TextView) rootView.findViewById(R.id.textView_game_detail_table_team2_set2));
        mTeam2PointsSets.add((TextView) rootView.findViewById(R.id.textView_game_detail_table_team2_set3));

        mSetTableRows = new ArrayList<TableRow>();
        mSetTableRows.add((TableRow) rootView.findViewById(R.id.tableRow_game_detail_set1));
        mSetTableRows.add((TableRow) rootView.findViewById(R.id.tableRow_game_detail_set2));
        mSetTableRows.add((TableRow) rootView.findViewById(R.id.tableRow_game_detail_set3));

        final EditText messageBox = (EditText) rootView.findViewById(R.id.editText_chat);
        final ImageButton buttonSendMessage = (ImageButton) rootView.findViewById(R.id.imageButton_send);

        // Hide send button if box is empty
        messageBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() <= 0) {
                    buttonSendMessage.setVisibility(View.INVISIBLE);
                    // set userTypingIndicator to false
                    mUserIsTypingRef.setValue(false);
                } else {
                    buttonSendMessage.setVisibility(View.VISIBLE);
                    // set userTypingIndicator to true
                    mUserIsTypingRef.setValue(true);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        buttonSendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (messageBox.length() > 0) {
                    sendMessage(messageBox.getText().toString(), v);
                }
                // Hide keyboard
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(messageBox.getWindowToken(), 0);
                messageBox.setText("");
            }
        });
    }

    private void sendMessage(String message, View view) {
        mGamesEventsRef.push().setValue(new GameEvent(message, Constants.ItemType.CHAT, mUserId));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mGameDetailListAdapter.cleanup();
        mActiveGameRef.removeEventListener(mActiveGameRefListener);
        mActiveGameSetsRef.removeEventListener(mActiveGameSetsRefListener);
    }
}
