package com.mobilis.android.nfc.domain;

public class ServerResponse {

	public enum Response{
		SUCCESS(0), ERROR(-1);
		private int status;
		private Response(int status){
			this.status = status;
		}
		public int getStatus() {
			return status;
		}
		
	}
	
	public Response serverResponse;
	private String response;

	public String getResponse() {
		return response;
	}

	public void setResponse(String response) {
		this.response = response;
	}
}
