package de.ustutt.iaas.cc.api;

import java.util.HashMap;
import java.util.Map;

/**
 * A note without a text, comprising only an ID and an author.
 * 
 * @author hauptfn
 *
 */
public class Note {

	private String id;
	private String author;

	public Note() {
		super();
	}

	public Note(String id, String author) {
		super();
		this.id = id;
		this.author = author;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public Map<String, String> getHashmap() {
		Map<String, String> map = new HashMap<String, String>();
		map.put("author", this.author);
		return map;
	}
}
