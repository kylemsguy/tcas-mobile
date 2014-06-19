/*
	This is an API for Two Cans and String
	
    Copyright (C) 2014  Kyle Zhou <kylezhou2002@hotmail.com>

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
    
 */
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
