package osfma.mcm.fhooe.at.livetickerprivate.ui.game.gameDetail;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
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
import osfma.mcm.fhooe.at.livetickerprivate.model.Chat;
import osfma.mcm.fhooe.at.livetickerprivate.model.Game;
import osfma.mcm.fhooe.at.livetickerprivate.model.GameEvent;
import osfma.mcm.fhooe.at.livetickerprivate.model.GameSet;
import osfma.mcm.fhooe.at.livetickerprivate.ui.game.gameManage.GameManageActivity;
import osfma.mcm.fhooe.at.livetickerprivate.utils.Constants;

public class GameDetailActivity extends AppCompatActivity {
    private static final String LOG_TAG = GameDetailActivity.class.getSimpleName();
    private Firebase mActiveGameRef;
    private Firebase mActiveGameSetsRef;
    private Firebase mGamesEventsRef;
    private Firebase mLastChildAdded;
    private String mGameId;
    private GameDetailListAdapter mGameDetailListAdapter;
    private ValueEventListener mActiveGameRefListener;
    private ChildEventListener mActiveGameSetsRefListener, mGamesEventsRefListener;
    private ListView mGameDetailListView;
    private TextView mTeam1Name, mTeam1Points, mTeam2Name, mTeam2Points;
    private ArrayList<TextView> mTeam1PointsSets, mTeam2PointsSets;
    private TextView mTeam1NameTable;
    private TextView mTeam2NameTable;
    private ArrayList<TableRow> mSetTableRows;
    private TableRow mHeadline;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_detail);

        /* Get the push ID from the extra passed by ShoppingListFragment */
        Intent intent = this.getIntent();
        mGameId = intent.getStringExtra(Constants.KEY_LIST_ID);
        if (mGameId == null) {
            /* No point in continuing without a valid ID. */
            finish();
            return;
        }

        /**
         * Create Firebase references
         */
        mActiveGameRef = new Firebase(Constants.FIREBASE_URL_GAMES).child(mGameId);
        mGamesEventsRef = new Firebase(Constants.FIREBASE_URL_GAMES_EVENTS).child(mGameId);
        mActiveGameSetsRef = new Firebase(Constants.FIREBASE_URL_GAMES).child(mGameId).child(Constants.FIREBASE_LOCATION_GAMES_GAMESETS);

        initializeScreen();

        /**
         * Setup the adapter
         */
        mGameDetailListAdapter = new GameDetailListAdapter(this, GameEvent.class,
                R.layout.single_game_event_list_item, mGamesEventsRef);
        /* Create ActiveListItemAdapter and set to listView */
        mGameDetailListView.setAdapter(mGameDetailListAdapter);

        mActiveGameRefListener = mActiveGameRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Game game = dataSnapshot.getValue(Game.class);
                mTeam1Name.setText(game.getTeam1());
                mTeam2Name.setText(game.getTeam2());

                mTeam1NameTable.setText(game.getTeam1());
                mTeam2NameTable.setText(game.getTeam2());
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

        mGamesEventsRefListener = mGamesEventsRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                mLastChildAdded = dataSnapshot.getRef();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

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

    private void initializeScreen() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mGameDetailListView = (ListView) findViewById(R.id.listView_game_events);

        mTeam1Name = (TextView) findViewById(R.id.textView_game_detail_team1);
        mTeam1Points = (TextView) findViewById(R.id.textView_game_detail_team1_points);
        mTeam2Name = (TextView) findViewById(R.id.textView_game_detail_team2);
        mTeam2Points = (TextView) findViewById(R.id.textView_game_detail_team2_points);

        mTeam1NameTable = (TextView) findViewById(R.id.textView_game_detail_table_team1);
        mTeam1PointsSets = new ArrayList<TextView>();
        mTeam1PointsSets.add((TextView) findViewById(R.id.textView_game_detail_table_team1_set1));
        mTeam1PointsSets.add((TextView) findViewById(R.id.textView_game_detail_table_team1_set2));
        mTeam1PointsSets.add((TextView) findViewById(R.id.textView_game_detail_table_team1_set3));

        mTeam2NameTable = (TextView) findViewById(R.id.textView_game_detail_table_team2);
        mTeam2PointsSets = new ArrayList<TextView>();
        mTeam2PointsSets.add((TextView) findViewById(R.id.textView_game_detail_table_team2_set1));
        mTeam2PointsSets.add((TextView) findViewById(R.id.textView_game_detail_table_team2_set2));
        mTeam2PointsSets.add((TextView) findViewById(R.id.textView_game_detail_table_team2_set3));

        mSetTableRows = new ArrayList<TableRow>();
        mSetTableRows.add((TableRow) findViewById(R.id.tableRow_game_detail_set1));
        mSetTableRows.add((TableRow) findViewById(R.id.tableRow_game_detail_set2));
        mSetTableRows.add((TableRow) findViewById(R.id.tableRow_game_detail_set3));

        final EditText message = (EditText) findViewById(R.id.editText_chat);

        ImageButton ButtonSendMessage = (ImageButton) findViewById(R.id.imageButton_send);
        ButtonSendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage(message.getText().toString(), v);
                // Hide keyboard
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(message.getWindowToken(), 0);
                message.setText("");
            }
        });
    }

    private void sendMessage(String message, View view) {
        if(mLastChildAdded != null) {
            mLastChildAdded.child("chatMessages").push().setValue(new Chat(message, "Anonymous"));
        } else{
            Snackbar.make(view, "Game not started yet!", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_game_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.action_manage_game){
            Intent intent = new Intent(getApplicationContext(), GameManageActivity.class);
            intent.putExtra(Constants.KEY_LIST_ID, mGameId);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // TODO FIX Error
        // Error because arrow button from ManageView guides to MainActivity and not DetailActivity
        /*
        mGameDetailListAdapter.cleanup();
        mActiveGameRef.removeEventListener(mActiveGameRefListener);
        mGamesEventsRef.removeEventListener(mGamesEventsRefListener);
        mActiveGameSetsRef.removeEventListener(mActiveGameSetsRefListener);
        */


    }
}
