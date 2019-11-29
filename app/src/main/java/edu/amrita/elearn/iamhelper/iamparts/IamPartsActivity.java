package edu.amrita.elearn.iamhelper.iamparts;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import edu.amrita.elearn.iamhelper.R;
import edu.amrita.elearn.iamhelper.model.StateModel;

public class IamPartsActivity extends AppCompatActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.iam_parts_item);

        Toolbar toolbar = findViewById(R.id.history_toolbar);
        toolbar.setTitle("Iam Parts");
        toolbar.setTitleTextColor(getResources().getColor(R.color.colorAccent));
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
        setSupportActionBar(toolbar);

        final RecyclerView recyclerView = findViewById(R.id.iam_recycler);
        recyclerView.setHasFixedSize(true);

        final LinearLayoutManager rvLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(rvLayoutManager);

        final IamPartsAdapter mAdapter = new IamPartsAdapter(this);
        recyclerView.setAdapter(mAdapter);
//        rvLayoutManager.scrollToPositionWithOffset();
        recyclerView.scrollToPosition(StateModel.getModel().getCurrentItemPosition()-1);
    }
}