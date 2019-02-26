package com.kmartin0.sceneformpolyexample.ui.dashboard;

import android.arch.lifecycle.Observer;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.kmartin0.sceneformpolyexample.R;
import com.kmartin0.sceneformpolyexample.api.PolyResponse;
import com.kmartin0.sceneformpolyexample.base.BaseActivity;
import com.kmartin0.sceneformpolyexample.databinding.ActivityDashboardBinding;
import com.kmartin0.sceneformpolyexample.ui.ar.ARActivity;
import com.kmartin0.sceneformpolyexample.ui.barcode.BarcodeActivity;
import com.kmartin0.sceneformpolyexample.util.Constants;
import com.kmartin0.sceneformpolyexample.util.DialogUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

public class DashboardActivity extends BaseActivity<ActivityDashboardBinding, DashboardViewModel> implements PolyAssetsAdapter.ItemClickListener {

	@BindView(R.id.modelRecyclerView)
	RecyclerView modelRecyclerView;

	private PolyAssetsAdapter modelAdapter;
	private List<PolyResponse> pollyModels = new ArrayList<>();

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initAssetListView();
		initObservers();
	}

	private void initObservers() {
		viewModel.getPollyAssets().observe(this, new Observer<List<PolyResponse>>() {
			@Override
			public void onChanged(@Nullable List<PolyResponse> polyResponses) {
				pollyModels.clear();
				pollyModels.addAll(polyResponses);
				modelAdapter.notifyDataSetChanged();
			}
		});

		viewModel.getError().observe(this, new Observer<String>() {
			@Override
			public void onChanged(@Nullable String s) {
				DialogUtils.showToast(DashboardActivity.this, s);
			}
		});
	}

	private void initAssetListView() {
		modelAdapter = new PolyAssetsAdapter(pollyModels, this);
		modelRecyclerView.setAdapter(modelAdapter);
		modelRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
		modelRecyclerView.setHasFixedSize(true);
		for (String id : Constants.ASSET_IDS) {
			viewModel.getPolyAsset(id);
		}
	}

	private void startArActivity(String assetUri) {
		Intent intent = new Intent(this, ARActivity.class);
		intent.putExtra(Constants.AR_MODEL_URI, Uri.parse(assetUri));
		startActivity(intent);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.action_qr_scanner: {
				new IntentIntegrator(this).setCaptureActivity(BarcodeActivity.class).initiateScan();
				return true;
			}
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		//We will get scan results here
		IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
		//check for null
		if (result != null) {
			if (result.getContents() == null) {
				DialogUtils.showToast(this, "Scan Cancelled");
			} else {
				startArActivity(result.getContents());
			}
		} else {
			// This is important, otherwise the result will not be passed to the fragment
			super.onActivityResult(requestCode, resultCode, data);
		}
	}

	@Override
	protected Integer getLayoutId() {
		return R.layout.activity_dashboard;
	}

	@Override
	protected void initViewModelBinding() {
		binding.setViewModel(viewModel);
	}

	@Override
	protected Class<DashboardViewModel> getVMClass() {
		return DashboardViewModel.class;
	}

	@Override
	public void onItemClick(PolyResponse pollyResponse) {
		startArActivity(pollyResponse.getGltf2Url());
	}

	@Override
	public void onPointerCaptureChanged(boolean hasCapture) {

	}
}