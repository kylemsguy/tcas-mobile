package com.kylemsguy.tcasmobile.backend;

public class Answer extends QAObject {
	
	private Question parent;
	private boolean read;

	public Answer(int id, String content, Question parent, boolean read) {
		super(id, content);
		this.parent = parent;
		this.read = read;
	}

}
