

package cn.itcast.wanxinp2p.common.domain;

public class RestResponse<T> {

    /**
     * 错误码
     */
    private int code;

    /**
     * 返回错误信息
     */
	private String msg;

    /**
     * 返回结果
     */
	private T result;

	public static <T> RestResponse<T> success() {
		return new RestResponse<T>();
	}

	public static <T> RestResponse<T> success(T result) {
		RestResponse<T> response = new RestResponse<T>();
		response.setResult(result);
		return response;
	}

	public static <T> RestResponse<T> failure(String msg) {
		RestResponse<T> response = new RestResponse<T>();
		response.setCode(-2);
		response.setMsg(msg);
		return response;
	}

	public RestResponse() {
		this(0, "");
	}

	public RestResponse(int code, String msg) {
		this.code = code;
		this.msg = msg;
	}

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public T getResult() {
		return result;
	}

	public void setResult(T result) {
		this.result = result;
	}

	public Boolean isSuccessful() {
		return this.code == 0;
	}

	@Override
	public String toString() {
		return "RestResponse [code=" + code + ", msg=" + msg + ", result="
				+ result + "]";
	}

}
