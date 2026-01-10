#!/usr/bin/env python3
"""
Fetch top ~100 popular works from OpenLibrary (by edition_count across several subjects)
and write them as BookRequest objects to dev-data/book-service/books.json so they
can be imported or mounted into the book-service for development.

Usage:
  python scripts/fetch_openlibrary_top100.py

The script is defensive: it queries several subjects, de-duplicates works by key,
then sorts by edition_count and keeps the top 100.
"""
try:
    import requests
    _HAS_REQUESTS = True
except Exception:
    requests = None
    _HAS_REQUESTS = False
import json
import os
from collections import OrderedDict
import urllib.request
import urllib.error
import time

OUTDIR = os.path.join(os.path.dirname(__file__), '..', 'dev-data', 'book-service')
OUTFILE = os.path.join(OUTDIR, 'books.json')

SUBJECTS = [
    'fiction', 'fantasy', 'mystery', 'romance', 'science_fiction',
    'historical_fiction', 'young_adult', 'children', 'nonfiction', 'bestsellers'
]

WORKS_PER_SUBJECT = 200
TARGET = 100
RETRIES = 3
BACKOFF_SECONDS = 2

def fetch_json(url, timeout=10):
    if _HAS_REQUESTS:
        try:
            r = requests.get(url, timeout=timeout, headers={'User-Agent': 'LittleBook-fetch-script/1.0 (+https://example.org)'})
            r.raise_for_status()
            return r.json()
        except Exception as e:
            raise
    else:
        try:
            req = urllib.request.Request(url, headers={'User-Agent': 'LittleBook-fetch-script/1.0 (+https://example.org)'})
            with urllib.request.urlopen(req, timeout=timeout) as resp:
                return json.load(resp)
        except urllib.error.URLError:
            raise

works_map = {}

for subject in SUBJECTS:
    url = f'https://openlibrary.org/subjects/{subject}.json?limit={WORKS_PER_SUBJECT}'
    try:
        print(f'Querying subject: {subject}')
        data = None
        for attempt in range(1, RETRIES + 1):
            try:
                data = fetch_json(url)
                break
            except Exception as e:
                if attempt < RETRIES:
                    wait = BACKOFF_SECONDS * attempt
                    print(f'  Attempt {attempt} failed for {subject}: {e} — retrying in {wait}s')
                    time.sleep(wait)
                    continue
                else:
                    raise
        works = data.get('works', []) if isinstance(data, dict) and data is not None else []
        for w in works:
            key = w.get('key')  # e.g. /works/OL123W
            if not key:
                continue
            if key in works_map:
                # keep the higher edition_count if seen across subjects
                existing = works_map[key]
                if w.get('edition_count', 0) > existing.get('edition_count', 0):
                    works_map[key] = w
            else:
                works_map[key] = w
    except Exception as e:
        print(f'Warning: failed to fetch subject {subject}: {e}')

print(f'Collected {len(works_map)} unique works, sorting by edition_count')

# Sort works by edition_count desc
sorted_works = sorted(works_map.values(), key=lambda x: x.get('edition_count', 0), reverse=True)
selected = sorted_works[:TARGET]

# Static curated fallback: produce realistic, well-known books for local/dev usage
def generate_static_books(target=TARGET):
    curated = [
        ("Pride and Prejudice", "Jane Austen"),
        ("1984", "George Orwell"),
        ("To Kill a Mockingbird", "Harper Lee"),
        ("The Great Gatsby", "F. Scott Fitzgerald"),
        ("Moby-Dick", "Herman Melville"),
        ("War and Peace", "Leo Tolstoy"),
        ("Ulysses", "James Joyce"),
        ("The Catcher in the Rye", "J.D. Salinger"),
        ("Brave New World", "Aldous Huxley"),
        ("The Hobbit", "J.R.R. Tolkien"),
        ("Crime and Punishment", "Fyodor Dostoevsky"),
        ("The Lord of the Rings", "J.R.R. Tolkien"),
        ("Alice's Adventures in Wonderland", "Lewis Carroll"),
        ("The Brothers Karamazov", "Fyodor Dostoevsky"),
        ("Jane Eyre", "Charlotte Bronte"),
        ("Wuthering Heights", "Emily Bronte"),
        ("The Odyssey", "Homer"),
        ("The Iliad", "Homer"),
        ("The Divine Comedy", "Dante Alighieri"),
        ("Les Miserables", "Victor Hugo"),
        ("Anna Karenina", "Leo Tolstoy"),
        ("The Picture of Dorian Gray", "Oscar Wilde"),
        ("Don Quixote", "Miguel de Cervantes"),
        ("One Hundred Years of Solitude", "Gabriel Garcia Marquez"),
        ("The Sound and the Fury", "William Faulkner"),
        ("A Tale of Two Cities", "Charles Dickens"),
        ("Great Expectations", "Charles Dickens"),
        ("The Grapes of Wrath", "John Steinbeck"),
        ("Slaughterhouse-Five", "Kurt Vonnegut"),
        ("The Alchemist", "Paulo Coelho"),
        ("The Little Prince", "Antoine de Saint-Exupery"),
        ("Fahrenheit 451", "Ray Bradbury"),
        ("The Kite Runner", "Khaled Hosseini"),
        ("Life of Pi", "Yann Martel"),
        ("The Chronicles of Narnia", "C.S. Lewis"),
        ("The Da Vinci Code", "Dan Brown"),
        ("The Girl with the Dragon Tattoo", "Stieg Larsson"),
        ("Memoirs of a Geisha", "Arthur Golden"),
        ("The Road", "Cormac McCarthy"),
        ("Beloved", "Toni Morrison"),
        ("Middlemarch", "George Eliot"),
        ("The Handmaid's Tale", "Margaret Atwood"),
        ("Gone Girl", "Gillian Flynn"),
        ("Sapiens: A Brief History of Humankind", "Yuval Noah Harari"),
        ("The Shining", "Stephen King"),
        ("It", "Stephen King"),
        ("Dracula", "Bram Stoker"),
        ("The Fault in Our Stars", "John Green"),
        ("The Hunger Games", "Suzanne Collins"),
        ("Dune", "Frank Herbert"),
        ("The Old Man and the Sea", "Ernest Hemingway"),
        ("The Sun Also Rises", "Ernest Hemingway"),
        ("A Clockwork Orange", "Anthony Burgess"),
        ("Rebecca", "Daphne du Maurier"),
        ("The Stranger", "Albert Camus"),
        ("The Trial", "Franz Kafka"),
        ("Heart of Darkness", "Joseph Conrad"),
        ("The Count of Monte Cristo", "Alexandre Dumas"),
        ("The Metamorphosis", "Franz Kafka"),
        ("The Secret Garden", "Frances Hodgson Burnett"),
        ("The Wind-Up Bird Chronicle", "Haruki Murakami"),
        ("Norwegian Wood", "Haruki Murakami"),
        ("A Confederacy of Dunces", "John Kennedy Toole"),
        ("The Name of the Rose", "Umberto Eco"),
        ("The Book Thief", "Markus Zusak"),
        ("The Silmarillion", "J.R.R. Tolkien"),
        ("The Martian", "Andy Weir"),
        ("Where the Crawdads Sing", "Delia Owens"),
        ("The Poisonwood Bible", "Barbara Kingsolver"),
        ("A Prayer for Owen Meany", "John Irving"),
        ("The Goldfinch", "Donna Tartt"),
        ("On the Road", "Jack Kerouac"),
        ("The Bell Jar", "Sylvia Plath"),
        ("The Color Purple", "Alice Walker"),
        ("The Help", "Kathryn Stockett"),
        ("The Time Traveler's Wife", "Audrey Niffenegger"),
        ("Atlas Shrugged", "Ayn Rand"),
        ("The Road to Serfdom", "F.A. Hayek"),
        ("The Art of War", "Sun Tzu"),
        ("The Prince", "Niccolo Machiavelli"),
        ("Thinking, Fast and Slow", "Daniel Kahneman"),
        ("A Brief History of Time", "Stephen Hawking"),
        ("Man's Search for Meaning", "Viktor E. Frankl"),
    ]
    out = []
    i = 0
    for title, author in curated:
        if i >= target:
            break
        book = OrderedDict()
        book['openlibraryId'] = None
        book['isbn10'] = None
        book['isbn13'] = None
        book['title'] = title
        book['subtitle'] = None
        book['authors'] = author
        book['publishYear'] = None
        book['coverUrl'] = None
        book['description'] = None
        book['subjects'] = None
        book['sourceData'] = json.dumps({'generated': True, 'title': title, 'authors': author})
        out.append(book)
        i += 1
    # If not enough curated items, pad with generic placeholders
    while i < target:
        book = OrderedDict()
        book['openlibraryId'] = None
        book['isbn10'] = None
        book['isbn13'] = None
        book['title'] = f'Placeholder Book {i+1}'
        book['subtitle'] = None
        book['authors'] = 'Unknown'
        book['publishYear'] = None
        book['coverUrl'] = None
        book['description'] = None
        book['subjects'] = None
        book['sourceData'] = json.dumps({'generated': True, 'placeholder': True})
        out.append(book)
        i += 1
    return out

# If we didn't gather enough works from subjects (OpenLibrary may be flaky),
# fallback to using the search API with common queries until we reach TARGET.
if len(works_map) < TARGET:
    print(f'Only collected {len(works_map)} works from subjects — falling back to search API')
    SEARCH_QUERIES = ['the', 'a', 'and', 'of', 'love', 'history', 'science', 'adventure', 'mystery']
    for q in SEARCH_QUERIES:
        if len(works_map) >= TARGET:
            break
        search_url = f'https://openlibrary.org/search.json?q={urllib.parse.quote(q)}&limit=200'
        try:
            print(f'Fallback search with query: {q}')
            data = fetch_json(search_url)
            docs = data.get('docs', []) if isinstance(data, dict) else []
            for d in docs:
                # docs may contain 'key' as '/works/OL...W'
                key = d.get('key')
                if not key and 'work_key' in d and d['work_key']:
                    key = '/works/' + d['work_key'][0]
                if not key:
                    continue
                if key in works_map:
                    continue
                # map fields into a compatible structure
                w = {
                    'key': key,
                    'title': d.get('title'),
                    'authors': [{'name': a} for a in (d.get('author_name') or [])],
                    'cover_id': d.get('cover_i'),
                    'first_publish_year': d.get('first_publish_year'),
                    'subject': d.get('subject') or [],
                    'edition_count': d.get('edition_count') or 0
                }
                works_map[key] = w
                if len(works_map) >= TARGET:
                    break
        except Exception as e:
            print(f'Fallback search failed for query {q}: {e}')

    # re-sort and select
    sorted_works = sorted(works_map.values(), key=lambda x: x.get('edition_count', 0), reverse=True)
    selected = sorted_works[:TARGET]

# If we didn't get enough works, use the static curated fallback which already
# matches the BookRequest shape used by the book-service loader.
if len(selected) < TARGET:
    print(f'Selected only {len(selected)} works — using static curated fallback to guarantee {TARGET} books')
    books_out = generate_static_books(TARGET)
else:
    # Map to BookRequest JSON shape expected by book-service BookRequest
    books_out = []
    for w in selected:
        key = w.get('key')
        ol_id = key.replace('/works/', '') if key else None
        title = w.get('title')
        authors = ','.join([a.get('name') for a in w.get('authors', []) if a.get('name')]) if w.get('authors') else None
        cover_id = w.get('cover_id')
        cover_url = f'https://covers.openlibrary.org/b/id/{cover_id}-L.jpg' if cover_id else None
        publish_year = w.get('first_publish_year')
        subjects = ','.join(w.get('subject', [])) if w.get('subject') else None
        source = w

        book = OrderedDict()
        book['openlibraryId'] = ol_id
        book['isbn10'] = None
        book['isbn13'] = None
        book['title'] = title or ''
        book['subtitle'] = None
        book['authors'] = authors
        book['publishYear'] = publish_year
        book['coverUrl'] = cover_url
        book['description'] = None
        book['subjects'] = subjects
        book['sourceData'] = json.dumps(source)

        books_out.append(book)

# Ensure output directory exists
os.makedirs(OUTDIR, exist_ok=True)
with open(OUTFILE, 'w', encoding='utf-8') as f:
    json.dump(books_out, f, ensure_ascii=False, indent=2)

print(f'Wrote {len(books_out)} books to {OUTFILE}')
print('Next steps:')
print('- Mount dev-data into book-service (already done for user-service).')
print('- Start containers: docker-compose up --build')
print("- Or POST these to the book API: POST /api/book with each object as JSON body")
