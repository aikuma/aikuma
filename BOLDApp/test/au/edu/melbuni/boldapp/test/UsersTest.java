package au.edu.melbuni.boldapp.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import au.edu.melbuni.boldapp.User;
import au.edu.melbuni.boldapp.Users;

import com.xtremelabs.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class UsersTest {
	
	static Users users;
	
	@Before
	public void setUp() throws Exception {
		users = new Users();
	}
	
	@Test
	public void makeTestRunnerHappy() {}
	
	@RunWith(RobolectricTestRunner.class)
	public static class WithoutUsers extends UsersTest {
		
		@Test
		public void get() { // delegates
			try {
				users.get(0);
			} catch (IndexOutOfBoundsException e) {
				assertNotNull(e);
			}
		}
		
		@Test
		public void size() { // delegates
			assertEquals(0, users.size());
		}
		
		@Test
		public void contains() { // delegates
			assertEquals(false, users.contains(new User()));
		}
		
	}
	
	@RunWith(RobolectricTestRunner.class)
	public static class WithUsers extends UsersTest {
		
		User currentUser;
		
		@Before
		public void setUp() throws Exception {
			super.setUp();
			
			currentUser = new User();
			
			users.add(currentUser);
		}
		
		@Test
		public void get() { // delegates
			assertEquals(currentUser, users.get(0));
		}
		
		@Test
		public void size() { // delegates
			assertEquals(1, users.size());
		}
		
		@Test
		public void contains() { // delegates
			assertEquals(true, users.contains(currentUser));
			assertEquals(false, users.contains(new User()));
		}
		
	}

}
