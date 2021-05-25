/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Georgi
 */
import java.io.Serializable;
// O structura pentru comunicarea intre server si client
// Tipuri de mesaje si descriere:
// tipul                     descrierea                      directia
// 0                        bloc difuzare server             server -> client
// 1                        clientul trimite blocul          client -> server
// 2                        serverul trimite id-ul clientului      server -> client
public class MessageStruct extends Object implements Serializable  {
    private static final long serialVersionUID = 3532734764930998421L;
	public int _code;
	public Object _content;
	
	public MessageStruct() {
		this._code = 0;
		this._content = null;
	}
	
	public MessageStruct(int code, Object content) {
		this._code = code;
		this._content = content;
	}
        public static void main(String[] args) {
            
        }
}
