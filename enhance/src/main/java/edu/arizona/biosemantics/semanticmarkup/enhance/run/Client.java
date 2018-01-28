package edu.arizona.biosemantics.semanticmarkup.enhance.run;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Logger;

import javax.ws.rs.client.AsyncInvoker;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.InvocationCallback;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.filter.LoggingFilter;
import org.glassfish.jersey.jackson.JacksonFeature;

import edu.arizona.biosemantics.common.log.LogLevel;
import edu.arizona.biosemantics.oto2.oto.shared.model.Bucket;
import edu.arizona.biosemantics.oto2.oto.shared.model.Collection;
import edu.arizona.biosemantics.common.context.shared.Context;
import edu.arizona.biosemantics.oto2.oto.shared.model.Label;
import edu.arizona.biosemantics.oto2.oto.shared.model.Term;
import edu.arizona.biosemantics.oto2.oto.shared.model.community.CommunityCollection;

public class Client implements AutoCloseable {

	private String url;
	private javax.ws.rs.client.Client client;
	private WebTarget target;	

	public Client(String url) {
		this.url = url;
	}
	
	public void open() {
		log(LogLevel.DEBUG, "Connect to " + url);
		client = ClientBuilder.newBuilder().withConfig(new ClientConfig()).register(JacksonFeature.class).build();
		client.register(new LoggingFilter(Logger.getAnonymousLogger(), true));
		
		//this doesn't seem to work for posts (among others), even though it is documented as such, use authentication header instead there
		//target = client.target(this.apiUrl).queryParam("apikey", this.apiKey);
		target = client.target(this.url);
	}
	
	public void close() {
		log(LogLevel.DEBUG, "Disconnect from " + url);
		client.close();
	}
	
	public Future<Void> post(Collection collection, boolean storeAsFallback) {
		return this.getPostInvoker(storeAsFallback).post(Entity.entity(collection, MediaType.APPLICATION_JSON), Void.class);
	}
	
	public void post(Collection collection, boolean storeAsFallback, InvocationCallback<Void> callback) {
		this.getPostInvoker(storeAsFallback).post(Entity.entity(collection, MediaType.APPLICATION_JSON), callback);
	}
	
	public Future<Collection> put(Collection collection) {
		return this.getPutInvoker().put(Entity.entity(collection, MediaType.APPLICATION_JSON), Collection.class);
	}
	
	public void put(Collection collection, InvocationCallback<List<Collection>> callback) {
		this.getPutInvoker().put(Entity.entity(collection, MediaType.APPLICATION_JSON), callback);
	}
	
	public Future<Collection> get(int id, String secret) {
		return this.getGetInvoker(id, secret).get(Collection.class);
	}
	
	public void get(int id, String secret, InvocationCallback<List<Collection>> callback) {
		this.getGetInvoker(id, secret).get(callback);
	}
	
	public Future<List<Context>> put(int collectionId, String secret, List<Context> contexts) {
		return this.getPutContextsInvoker(collectionId, secret).put(Entity.entity(contexts, MediaType.APPLICATION_JSON), new GenericType<List<Context>>(){});
	}
	
	public void put(int collectionId, String secret, List<Context> contexts, InvocationCallback<List<Context>> callback) {
		this.getPutContextsInvoker(collectionId, secret).put(Entity.entity(contexts, MediaType.APPLICATION_JSON), callback);
	}
	
	public Future<CommunityCollection> getCommunityCollection(String type) {
		return this.getGetCommunityCollectionInvoker(type).get(CommunityCollection.class);
	}
	
	public void getCommunityCollection(String type, InvocationCallback<CommunityCollection> callback) {
		this.getGetCommunityCollectionInvoker(type).get(callback);
	}

	private AsyncInvoker getGetCommunityCollectionInvoker(String type) {
		return target.path("rest").path("oto").path("community").path(type).request(MediaType.APPLICATION_JSON).async();
	}
	
	private AsyncInvoker getPostInvoker(boolean storeAsFallback) {
		return target.path("rest").path("oto").path("collection").queryParam("storeAsFallback", storeAsFallback)
				.request(MediaType.APPLICATION_JSON).async();
	}
	
	private AsyncInvoker getPutInvoker() {
		return target.path("rest").path("oto").path("collection").request(MediaType.APPLICATION_JSON).async();
	}
	
	private AsyncInvoker getGetInvoker(int id, String secret) {
		return target.path("rest").path("oto").path("collection").path(String.valueOf(id))
				.queryParam("secret", secret).request(MediaType.APPLICATION_JSON).async();
	}
	
	private AsyncInvoker getPutContextsInvoker(int collectionId, String secret) {
		return target.path("rest").path("oto").path("context").path(String.valueOf(collectionId)).queryParam("secret", secret).request(MediaType.APPLICATION_JSON).async();
	}
	
	/**
	 * @param args
	 * @throws ExecutionException 
	 * @throws InterruptedException 
	 */
	public static void main(String[] args) throws InterruptedException, ExecutionException {
		Client client = new Client("http://127.0.0.1:59512/");	
		client.open();
		
		/*Collection collection = new Collection("name", "type", "my secret");
		Future<Collection> col = client.put(collection);
		col.get();
		
		Future<Collection> colFu = client.get(1, "my secret");
		colFu.get();
		*/
		
		List<Context> contexts = new LinkedList<Context>();
		contexts.add(new Context(1, "src", "aaaa"));
		contexts.add(new Context(2, "src2", "aaaa2"));
		
		Future<List<Context>> result = client.put(17, "30", contexts);
		result.get();
		
		
		/*
		Future<Collection> collectionFuture = client.put(createSampleCollection());
		Collection collection = collectionFuture.get();
		collectionFuture = client.get(String.valueOf(collection.getId()), collection.getSecret());
		System.out.println(collectionFuture.get());
		*/
		client.close();
	}
	
	public static Collection createSampleCollection() {
		List<Bucket> buckets = new LinkedList<Bucket>();
		Bucket b = new Bucket();
		Bucket b2 = new Bucket();
		Bucket b3 = new Bucket();
		Term t1 = new Term();
		t1.setTerm("leaf1");
		Term t2 = new Term();
		t2.setTerm("stem");
		Term t3 = new Term();
		t3.setTerm("apex");
		Term t4 = new Term();
		t4.setTerm("root");
		Term t5 = new Term();
		t5.setTerm("sepal");
		b.addTerm(t1);
		b.addTerm(t2);
		b.addTerm(t3);
		b.addTerm(t4);
		b.addTerm(t5);
		buckets.add(b);
		b.setName("structures");
		Term c1 = new Term("length");
		Term c2 = new Term("color");
		b2.addTerm(c1);
		b2.addTerm(c2);
		b2.setName("characters");
		b3.setName("others");
		Term o1 = new Term("asdfg");
		b3.addTerm(o1);
		buckets.add(b2);
		buckets.add(b3);
		
		Collection collection = new Collection();
		collection.setName("My test");
		collection.setBuckets(buckets);
		
		/*List<Label> labels = new LinkedList<Label>();
		Label l0 = new Label();
		l0.setName("structure");
		
		Label l1 = new Label();
		l1.setName("arrangement");
		
		Label l2 = new Label();
		l2.setName("architecture");
		
		Label l3 = new Label();
		l3.setName("coloration");		
		
		labels.add(l0);
		labels.add(l1);
		labels.add(l2);
		labels.add(l3);
		collection.setLabels(labels);*/
		
		collection.setSecret("my secret");
		return collection;
	}
}