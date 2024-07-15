from flask import Flask, request, jsonify
import internetarchive
import logging

app = Flask(__name__)

def search_comics(query, limit=10):
    app.logger.debug(f"Searching comics with query: {query}")
    search = internetarchive.search_items(f'collection:comics AND title:({query})')
    results = []
    count = 0
    for result in search:
        if count >= limit:
            break
        identifier = result['identifier']
        app.logger.debug(f"Found result with identifier: {identifier}")
        try:
            item = internetarchive.get_item(identifier)
            metadata = item.metadata
            title = metadata.get('title', 'No Title')
            description = metadata.get('description', 'No Description')
            year = metadata.get('year', 'Unknown')
            language = metadata.get('language', 'Unknown')
            collection = metadata.get('collection', 'Unknown')
            if isinstance(collection, list):
                collection = ', '.join(collection)
            subject = metadata.get('subject', 'Unknown')
            if isinstance(subject, list):
                subject = ', '.join(subject)
            results.append({
                'id': result['identifier'],
                'title': title,
                'identifier': identifier,
                'description': description,
                'thumbnail': f"https://archive.org/services/img/{identifier}",
                'year': year,
                'language': language,
                'collection': collection,
                'subject': subject
            })
            app.logger.debug(f"Added comic to results: {title}")
            count += 1
        except Exception as e:
            app.logger.error(f"Error retrieving item {identifier}: {e}")
    app.logger.debug(f"Found {len(results)} results")
    return results

def get_comic_pdf(identifier):
    app.logger.debug(f"Getting PDF for comic with identifier: {identifier}")
    try:
        item = internetarchive.get_item(identifier)
        files = item.get_files()
        for file in files:
            app.logger.debug(f"Checking file: {file.name}")
            if file.name.endswith('.pdf'):
                app.logger.debug(f"Found PDF: {file.url}")
                return file.url
    except Exception as e:
        app.logger.error(f"Error retrieving files for item {identifier}: {e}")
    app.logger.debug("No PDF found")
    return None

def get_comic_metadata(field, start=0, limit=20):
    search = internetarchive.search_items(f'collection:comics')
    metadata = {}
    count = 0
    for i, result in enumerate(search):
        if i < start:
            continue
        if count >= limit:
            break
        try:
            item = internetarchive.get_item(result['identifier'])
            field_value = item.metadata.get(field, 'Unknown')
            if isinstance(field_value, list):
                for value in field_value:
                    metadata[value] = metadata.get(value, 0) + 1
            else:
                metadata[field_value] = metadata.get(field_value, 0) + 1
        except Exception as e:
            app.logger.error(f"Error retrieving metadata for item {result['identifier']}: {e}")
        count += 1
    return metadata

@app.route('/comics', methods=['GET'])
def get_comics():
    query = request.args.get('query', default='', type=str)
    limit = request.args.get('limit', default=10, type=int)
    if not query:
        return jsonify({'error': 'Query parameter is required'}), 400
    try:
        comics = search_comics(query, limit)
        return jsonify(comics)
    except Exception as e:
        app.logger.error(f'Error searching comics: {e}')
        return jsonify({'error': 'Internal Server Error'}), 500

@app.route('/comic/pdf', methods=['GET'])
def get_comic_pdf_endpoint():
    identifier = request.args.get('identifier', default='', type=str)
    if not identifier:
        return jsonify({'error': 'Identifier parameter is required'}), 400
    try:
        pdf_url = get_comic_pdf(identifier)
        if not pdf_url:
            return jsonify({'error': 'PDF not found'}), 404
        return jsonify({'pdf_url': pdf_url})
    except Exception as e:
        app.logger.error(f'Error getting comic PDF: {e}')
        return jsonify({'error': 'Internal Server Error'}), 500

@app.route('/comics/metadata/<field>', methods=['GET'])
def get_comic_metadata_endpoint(field):
    valid_fields = ['year', 'language', 'collection', 'subject']
    if field not in valid_fields:
        return jsonify({'error': f'Invalid field. Valid fields are: {", ".join(valid_fields)}'}), 400
    try:
        start = int(request.args.get('start', 0))
        limit = int(request.args.get('limit', 30))
        metadata = get_comic_metadata(field, start=start, limit=limit)
        return jsonify(metadata)
    except Exception as e:
        app.logger.error(f'Error getting metadata for {field}: {e}')
        return jsonify({'error': 'Internal Server Error'}), 500

@app.route('/comics/collection', methods=['GET'])
def get_comics_by_collection():
    collection_id = request.args.get('collectionId', default='', type=str)
    start = request.args.get('start', default=0, type=int)
    limit = request.args.get('limit', default=20, type=int)
    if not collection_id:
        return jsonify({'error': 'CollectionId parameter is required'}), 400
    try:
        search = internetarchive.search_items(f'collection:{collection_id}')
        results = []
        count = 0
        for i, result in enumerate(search):
            if i < start:
                continue
            if count >= limit:
                break
            identifier = result['identifier']
            try:
                item = internetarchive.get_item(identifier)
                metadata = item.metadata
                title = metadata.get('title', 'No Title')
                description = metadata.get('description', 'No Description')
                year = metadata.get('year', 'Unknown')
                language = metadata.get('language', 'Unknown')
                collection = metadata.get('collection', 'Unknown')
                if isinstance(collection, list):
                    collection = ', '.join(collection)
                subject = metadata.get('subject', 'Unknown')
                if isinstance(subject, list):
                    subject = ', '.join(subject)
                results.append({
                    'id': result['identifier'],
                    'title': title,
                    'identifier': identifier,
                    'description': description,
                    'thumbnail': f"https://archive.org/services/img/{identifier}",
                    'year': year,
                    'language': language,
                    'collection': collection,
                    'subject': subject
                })
                count += 1
            except Exception as e:
                app.logger.error(f"Error retrieving item {identifier}: {e}")
        app.logger.debug(f"Found {len(results)} results")
        return jsonify(results)
    except Exception as e:
        app.logger.error(f'Error searching comics: {e}')
        return jsonify({'error': 'Internal Server Error'}), 500

@app.route('/comics/advanced_search', methods=['GET'])
def get_comics_by_advanced_search():
    collection = request.args.get('collection', default='', type=str)
    language = request.args.get('language', default='', type=str)
    year = request.args.get('year', default='', type=str)
    genre = request.args.get('genre', default='', type=str)
    limit = request.args.get('limit', default=20, type=int)

    search_query = ' AND '.join(
        f'{field}:({value})' for field, value in [
            ('collection', collection),
            ('language', language)
            # ('year', year),
            # ('subject', genre)
        ] if value
    )

    app.logger.debug(f"Performing advanced search with query: {search_query}")

    try:
        results = search_comics(search_query, limit)
        app.logger.debug(f"Advanced search results: {results}")
        return jsonify(results)
    except Exception as e:
        app.logger.error(f'Error performing advanced search: {e}')
        return jsonify({'error': 'Internal Server Error'}), 500

@app.route('/comics/id', methods=['GET'])
def get_comics_by_id():
    comics_id = request.args.get('id', default='', type=str)
    if not comics_id:
        return jsonify({'error': 'ID parameter is required'}), 400
    try:
        search = internetarchive.search_items(f'identifier:{comics_id}')
        results = []
        for result in search:
            identifier = result['identifier']
            item = internetarchive.get_item(identifier)
            metadata = item.metadata
            title = metadata.get('title', 'No Title')
            description = metadata.get('description', 'No Description')
            year = metadata.get('year', 'Unknown')
            language = metadata.get('language', 'Unknown')
            collection = metadata.get('collection', 'Unknown')
            if isinstance(collection, list):
                collection = ', '.join(collection)
            subject = metadata.get('subject', 'Unknown')
            if isinstance(subject, list):
                subject = ', '.join(subject)
            results.append({
                'id': result['identifier'],
                'title': title,
                'identifier': identifier,
                'description': description,
                'thumbnail': f"https://archive.org/services/img/{identifier}",
                'year': year,
                'language': language,
                'collection': collection,
                'subject': subject
            })
        return jsonify(results)
    except Exception as e:
        app.logger.error(f'Error searching comics by ID: {e}')
        return jsonify({'error': 'Internal Server Error'}), 500

if __name__ == '__main__':
    logging.basicConfig(level=logging.DEBUG)
    app.run(host='0.0.0.0', port=5000, debug=True)