package com.littlebook.admin.service;

import com.littlebook.admin.dto.LoginRecordRequest;
import com.littlebook.admin.dto.SetLastLoginRequest;
import com.littlebook.admin.dto.SetTotalLoginsRequest;
import com.littlebook.admin.entity.LoginEvent;
import com.littlebook.admin.entity.UserLoginStats;
import com.littlebook.admin.repository.LoginEventRepository;
import com.littlebook.admin.repository.ReviewActivityRepository;
import com.littlebook.admin.repository.UserLoginStatsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StatsServiceTest {

    @Mock
    private UserLoginStatsRepository userRepo;

    @Mock
    private LoginEventRepository loginRepo;

    @Mock
    private ReviewActivityRepository reviewRepo;

    private ObjectProvider<UserClient> userClientProvider;

    private UserClient userClient;

    private StatsService service;

    private UUID userId;

    @BeforeEach
    void init() {
        userId = UUID.randomUUID();
        // default userClient: returns empty (can be replaced per-test)
        userClient = new UserClient(null, "http://unused") {
            @Override
            public Optional<Map<String, Object>> getUserById(UUID id) {
                return Optional.empty();
            }
        };

        // provider that returns the current userClient field
        userClientProvider = new ObjectProvider<>() {
            @Override
            public UserClient getObject(Object... args) {
                return userClient;
            }

            @Override
            public UserClient getObject() {
                return userClient;
            }

            @Override
            public UserClient getIfAvailable() {
                return userClient;
            }

            @Override
            public UserClient getIfUnique() {
                return userClient;
            }

            @Override
            public Iterator<UserClient> iterator() {
                return Collections.emptyIterator();
            }
        };

        // create service with the provider
        service = new StatsService(userRepo, loginRepo, reviewRepo, userClientProvider);
    }

    @Test
    void summary_shouldAggregateCounts() {
        when(userRepo.count()).thenReturn(2L);
        when(loginRepo.count()).thenReturn(3L);
        when(reviewRepo.countByEventType("review_created")).thenReturn(5L);
        when(reviewRepo.countByEventType("review_liked")).thenReturn(1L);

        Map<String, Object> res = service.summary();

        assertEquals(2L, res.get("nbUsers"));
        assertEquals(3L, res.get("nbLogins"));
        assertEquals(6L, res.get("nbReviews"));
        assertEquals(1L, res.get("nbLikes"));
    }

    @Test
    void listAndGetMethods_delegateToRepositories() {
        UserLoginStats s = new UserLoginStats();
        s.setUserId(userId);

        when(userRepo.findAll()).thenReturn(List.of(s));
        when(userRepo.findById(userId)).thenReturn(Optional.of(s));
        when(loginRepo.findAll()).thenReturn(List.of());
        when(reviewRepo.findAll()).thenReturn(List.of());

        assertEquals(1, service.listUsers().size());
        assertTrue(service.getUser(userId).isPresent());
        assertNotNull(service.listLoginEvents());
        assertNotNull(service.listReviewActivities());
    }

    @Test
    void recordLogin_createsLoginEventAndStats_whenMissing() {
        when(userRepo.findById(userId)).thenReturn(Optional.empty());
        when(loginRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(userRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        LoginRecordRequest req = new LoginRecordRequest();
        req.setLoggedInAt(now);
        req.setProvider("p");
        req.setIpAddress("1.2.3.4");
        req.setUserAgent("ua");

        ArgumentCaptor<LoginEvent> evCap = ArgumentCaptor.forClass(LoginEvent.class);
        ArgumentCaptor<UserLoginStats> statsCap = ArgumentCaptor.forClass(UserLoginStats.class);

        UserLoginStats out = service.recordLogin(userId, req);

        verify(loginRepo).save(evCap.capture());
        LoginEvent ev = evCap.getValue();
        assertEquals(userId, ev.getUserId());
        assertEquals("p", ev.getProvider());
        assertEquals("1.2.3.4", ev.getIpAddress());

        verify(userRepo).save(statsCap.capture());
        UserLoginStats saved = statsCap.getValue();
        assertEquals(1, saved.getTotalLogins());
        assertEquals(now, saved.getFirstLoginAt());
        assertEquals(now, saved.getLastLoginAt());
        assertEquals(0.0, saved.getAvgDaysBetweenLogins(), 1e-6);
        assertEquals(saved.getTotalLogins(), out.getTotalLogins());
    }

    @Test
    void recordLogin_updatesExistingAndComputesAvg() {
        LocalDateTime first = LocalDateTime.now().minusDays(2).truncatedTo(ChronoUnit.SECONDS);
        UserLoginStats existing = new UserLoginStats();
        existing.setUserId(userId);
        existing.setTotalLogins(1);
        existing.setFirstLoginAt(first);
        existing.setLastLoginAt(first);

        when(userRepo.findById(userId)).thenReturn(Optional.of(existing));
        when(loginRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(userRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        LocalDateTime second = first.plusDays(2);
        LoginRecordRequest req = new LoginRecordRequest();
        req.setLoggedInAt(second);

        UserLoginStats out = service.recordLogin(userId, req);

        assertEquals(2, out.getTotalLogins());
        assertEquals(first, out.getFirstLoginAt());
        assertEquals(second, out.getLastLoginAt());
        assertEquals(2.0, out.getAvgDaysBetweenLogins(), 1e-6);
    }

    @Test
    void incrementLogin_callsRecordLogin() {
        // avoid Mockito spy on StatsService (some JVMs block inline mocking of local classes)
        class TestStatsService extends StatsService {
            boolean called = false;

            TestStatsService(UserLoginStatsRepository ur, LoginEventRepository lr, ReviewActivityRepository rr, ObjectProvider<UserClient> p) {
                super(ur, lr, rr, p);
            }

            @Override
            public UserLoginStats recordLogin(UUID userId, LoginRecordRequest req) {
                called = true;
                return new UserLoginStats();
            }
        }

        TestStatsService testSvc = new TestStatsService(userRepo, loginRepo, reviewRepo, userClientProvider);
        UserLoginStats out = testSvc.incrementLogin(userId);
        assertNotNull(out);
        assertTrue(testSvc.called);
    }

    @Test
    void setLastLogin_createsWhenMissing_andUpdatesWhenPresent() {
        when(userRepo.findById(userId)).thenReturn(Optional.empty());
        when(userRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));
        LocalDateTime at = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        SetLastLoginRequest req = new SetLastLoginRequest();
        req.setLastLoginAt(at);

        UserLoginStats created = service.setLastLogin(userId, req);
        assertEquals(at, created.getLastLoginAt());
        assertEquals(at, created.getFirstLoginAt());

        UserLoginStats existing = new UserLoginStats();
        existing.setUserId(userId);
        existing.setFirstLoginAt(LocalDateTime.now().minusDays(5));
        existing.setTotalLogins(3);
        when(userRepo.findById(userId)).thenReturn(Optional.of(existing));
        when(userRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        SetLastLoginRequest req2 = new SetLastLoginRequest();
        LocalDateTime at2 = LocalDateTime.now();
        req2.setLastLoginAt(at2);
        UserLoginStats updated = service.setLastLogin(userId, req2);
        assertEquals(at2, updated.getLastLoginAt());
    }

    @Test
    void setTotalLogins_updatesAndComputesAvg() {
        LocalDateTime first = LocalDateTime.now().minusDays(4).truncatedTo(ChronoUnit.SECONDS);
        LocalDateTime last = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        UserLoginStats existing = new UserLoginStats();
        existing.setUserId(userId);
        existing.setFirstLoginAt(first);
        existing.setLastLoginAt(last);

        when(userRepo.findById(userId)).thenReturn(Optional.of(existing));
        when(userRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        SetTotalLoginsRequest req = new SetTotalLoginsRequest();
        req.setTotalLogins(3);
        UserLoginStats out = service.setTotalLogins(userId, req);
        assertEquals(3, out.getTotalLogins());
        assertEquals(2.0, out.getAvgDaysBetweenLogins(), 1e-6);
    }

    @Test
    void listUsersEnriched_worksWithProviderNull_andWithProvider_andHandlesExceptions() {
        UserLoginStats s = new UserLoginStats();
        s.setUserId(userId);
        s.setTotalLogins(2);
        when(userRepo.findAll()).thenReturn(List.of(s));
        // provider null: create service with a provider that returns null
        ObjectProvider<UserClient> nullProvider = new ObjectProvider<>() {
            @Override
            public UserClient getObject(Object... args) { return null; }
            @Override
            public UserClient getObject() { return null; }
            @Override
            public UserClient getIfAvailable() { return null; }
            @Override
            public UserClient getIfUnique() { return null; }
            @Override
            public Iterator<UserClient> iterator() { return Collections.emptyIterator(); }
        };
        StatsService svcNull = new StatsService(userRepo, loginRepo, reviewRepo, nullProvider);
        List<Map<String, Object>> res = svcNull.listUsersEnriched();
        assertEquals(1, res.size());
        assertEquals(userId, res.get(0).get("userId"));

        // provider present: userClient returns a map with email
        UserClient presentClient = new UserClient(null, "http://unused") {
            @Override
            public Optional<Map<String, Object>> getUserById(UUID id) {
                return Optional.of(Map.of("email", "a@b.c"));
            }
        };
        ObjectProvider<UserClient> presentProvider = new ObjectProvider<>() {
            @Override
            public UserClient getObject(Object... args) { return presentClient; }
            @Override
            public UserClient getObject() { return presentClient; }
            @Override
            public UserClient getIfAvailable() { return presentClient; }
            @Override
            public UserClient getIfUnique() { return presentClient; }
            @Override
            public Iterator<UserClient> iterator() { return Collections.emptyIterator(); }
        };
        StatsService svcPresent = new StatsService(userRepo, loginRepo, reviewRepo, presentProvider);
        List<Map<String, Object>> res2 = svcPresent.listUsersEnriched();
        assertEquals("a@b.c", res2.get(0).get("email"));

        // provider throws: userClient throws on call
        UserClient throwingClient = new UserClient(null, "http://unused") {
            @Override
            public Optional<Map<String, Object>> getUserById(UUID id) {
                throw new RuntimeException("boom");
            }
        };
        ObjectProvider<UserClient> throwingProvider = new ObjectProvider<>() {
            @Override
            public UserClient getObject(Object... args) { return throwingClient; }
            @Override
            public UserClient getObject() { return throwingClient; }
            @Override
            public UserClient getIfAvailable() { return throwingClient; }
            @Override
            public UserClient getIfUnique() { return throwingClient; }
            @Override
            public Iterator<UserClient> iterator() { return Collections.emptyIterator(); }
        };
        StatsService svcThrow = new StatsService(userRepo, loginRepo, reviewRepo, throwingProvider);
        List<Map<String, Object>> res3 = svcThrow.listUsersEnriched();
        assertEquals(userId, res3.get(0).get("userId"));
    }
}
