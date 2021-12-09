package de.ustutt.iaas.cc.core;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import com.google.api.core.ApiFuture;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.SetOptions;
import com.google.cloud.firestore.WriteResult;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.ustutt.iaas.cc.api.Note;
import de.ustutt.iaas.cc.api.NoteWithText;

/**
 * DAO implementation that stores notes as entities in Google Datastore (NoSQL).
 * 
 * @author hauptfn
 *
 */
public class GoogleDatastoreNotebookDAO implements INotebookDAO {

    private final static Logger logger = LoggerFactory.getLogger(GoogleDatastoreNotebookDAO.class);

    private final Firestore db;
    
    public GoogleDatastoreNotebookDAO() {
    	super();
        try {
    	    GoogleCredentials credentials = GoogleCredentials.getApplicationDefault();
            FirebaseOptions options = new FirebaseOptions.Builder().setCredentials(credentials).setProjectId("cloudcomputing-334610").build();
            FirebaseApp.initializeApp(options);
        }catch(Exception e) {
            logger.error("Error initialising", e.getMessage());
        }finally {
            this.db = FirestoreClient.getFirestore();
        }
    }

    @Override
    public Set<Note> getNotes() {
    	Set<Note> result = new HashSet<Note>();
        ApiFuture<QuerySnapshot> query = db.collection("notes").get();
        try {
            QuerySnapshot snapshot = query.get();
            List<QueryDocumentSnapshot> docs = snapshot.getDocuments();
            for(QueryDocumentSnapshot doc: docs) {
                if (doc.contains("text")) {
                    result.add(new NoteWithText(doc.getId(), doc.getString("author"), doc.getString("text")));
                } else {
                    result.add(new Note(doc.getId(), doc.getString("author")));
                }
            }
        }catch(InterruptedException ie) {
            logger.error("Query got interrupted: ", ie.getMessage());
        }catch(ExecutionException ie) {
            logger.error("Exception: ", ie.getMessage());
        }

    	return result;
    }

    @Override
    public NoteWithText getNote(String noteID) {
    	NoteWithText result = null;
		ApiFuture<DocumentSnapshot> snapshot = db.collection("notes").document(noteID).get();
        try {
            DocumentSnapshot doc = snapshot.get();
            if (doc.exists()) {
                if (doc.contains("text")) {
                    result = new NoteWithText(doc.getId(), doc.getString("author"), doc.getString("text"));
                } else {
                    result = new NoteWithText(doc.getId(), doc.getString("author"), "");
                }
            }  else { 
                logger.info("Document does not exist");
            }
        }catch(InterruptedException ie) {
            logger.error("Query got interrupted: ", ie.getMessage());
        }catch(ExecutionException ie) {
            logger.error("Exception: ", ie.getMessage());
        }
    	return result;
    }

    @Override
    public NoteWithText createOrUpdateNote(NoteWithText note) {
    	NoteWithText result = null;
    	try {
            ApiFuture<WriteResult> future = db.collection("notes").document(note.getId()).set(note.getHashmap(), SetOptions.merge());
            WriteResult writeResult= future.get();
            logger.info("Data created or Updated: ", writeResult.getUpdateTime());
            result = note;
        }catch(InterruptedException ie) {
            logger.error("Query got interrupted: ", ie.getMessage());
        }catch(ExecutionException ie) {
            logger.error("Exception: ", ie.getMessage());
        }
    	return result;
    }

    @Override
    public void deleteNote(String noteID) {
        try {
    	    ApiFuture<WriteResult> result = db.collection("notes").document(noteID).delete();
            logger.info("Data deleted: ", result.get().getUpdateTime());
        }catch(InterruptedException ie) {
            logger.error("Query got interrupted: ", ie.getMessage());
        }catch(ExecutionException ie) {
            logger.error("Exception: ", ie.getMessage());
        }
    }

}
