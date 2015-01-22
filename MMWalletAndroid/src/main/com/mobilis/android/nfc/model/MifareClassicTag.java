package com.mobilis.android.nfc.model;

import android.nfc.tech.MifareClassic;

public class MifareClassicTag {

	private MifareClassic mfc;
	private int dataSectorIndex;
	private int backupSectorIndex;
	private String tagUId;
	private String dataBalance;
	private String backupBalance;
	private boolean isNewTag;
	private boolean hasBackupSector;
	
	public MifareClassic getMfc() {
		return mfc;
	}
	public void setMfc(MifareClassic mfc) {
		this.mfc = mfc;
	}
	public int getDataSectorIndex() {
		return dataSectorIndex;
	}
	public void setDataSectorIndex(int dataSectorIndex) {
		this.dataSectorIndex = dataSectorIndex;
	}
	public int getBackupSectorIndex() {
		return backupSectorIndex;
	}
	public void setBackupSectorIndex(int backupSectorIndex) {
		this.backupSectorIndex = backupSectorIndex;
	}
	public String getTagUId() {
		return tagUId;
	}
	public void setTagUId(String tagUId) {
		this.tagUId = tagUId;
	}
	public String getDataBalance() {
		return dataBalance;
	}
	public void setDataBalance(String dataBalance) {
		this.dataBalance = dataBalance;
	}
	public String getBackupBalance() {
		return backupBalance;
	}
	public void setBackupBalance(String backupBalance) {
		this.backupBalance = backupBalance;
	}
	public boolean isNewTag() {
		return isNewTag;
	}
	public void setNewTag(boolean isNewTag) {
		this.isNewTag = isNewTag;
	}
	public boolean hasBackupSector() {
		return hasBackupSector;
	}
	public void setHasBackupSector(boolean hasBackupSector) {
		this.hasBackupSector = hasBackupSector;
	}
	
}
