let map;
let editMarker;

/**
 * If user is logged in, displays the commenting form, otherwise prompts the user to log in.
 */
async function handleLogin() {
  fetch('/status').then(response => response.json()).then((isLoggedIn) => {
    if (!isLoggedIn) {
      window.location.replace("/login");
    }
  });
}

/**
 * Gets comments from the server.
 */
async function getComments(limit) {
  const historyEl = document.getElementById('comments-history');
  historyEl.innerHTML = ''; //clears the table of previous comments
  fetch('/comments?comment-limit=' + limit).then(response => response.json()).then((comments) => {
    comments.forEach((comment) => {
      historyEl.appendChild(createListElement(comment));
    });
  });
}

/** Creates an <li> element containing text. */
function createListElement(comment) {
  const liElement = document.createElement('li');
  liElement.innerText = comment.text + ' - ' + comment.email;
  return liElement;
}

/**
 * Deletes comments from the server.
 */
async function deleteComments() {
  const request = new Request('/delete-comments', {method: 'POST', body: '{}'});
  fetch(request).then(response => {
    const historyEl = document.getElementById('comments-history');
    historyEl.innerHTML = ''; //clears the table of displayed previous comments
  });
}

/** Creates a map that allows users to add markers. */
function initMap() {
  const usaLat = 38.5949;
  const usaLng = -94.8923;
  const defaultZoom = 4;
  map = new google.maps.Map(
      document.getElementById('map'),
      {center: {lat: usaLat, lng: usaLng}, zoom: defaultZoom});
  map.addListener('click', (event) => {
    postMarker(event.latLng.lat(), event.latLng.lng());
    fetchMarkers();
  });
}

/** Fetches markers from the backend and adds them to the map. */
function fetchMarkers() {
  fetch('/markers').then(response => response.json()).then((markers) => {
    markers.forEach(
        (marker) => {
            createMarkerForDisplay(marker.lat, marker.lng, marker.content)});
  });
}

/** Creates a marker that shows a read-only info window when clicked. */
function createMarkerForDisplay(lat, lng, content) {
  const marker =
      new google.maps.Marker({position: {lat: lat, lng: lng}, map: map});
  const infoWindow = new google.maps.InfoWindow({content: content});
  marker.addListener('click', () => {
    infoWindow.open(map, marker);
  });
}

/** Sends a marker to the backend for saving. */
function postMarker(lat, lng) {
  const params = new URLSearchParams();
  params.append('lat', lat);
  params.append('lng', lng);
  fetch('/markers', {method: 'POST', body: params});
}

/**
 * Leads user to a selected project.
 */
function chooseFeaturedProj() {
  const urls = [
      'https://github.com/georgezheng999/Depmap-Data-Scraping', 
      'https://github.mit.edu/MEDSL/primary-precincts', 
      'https://github.com/georgezheng999/Pac-Man', 
      '', //empty string corresponding to portfolio entry, to effectively refresh the page.
      'https://github.com/georgezheng999/Chess-Two-Player', 
      'https://github.com/georgezheng999/APCS-Spring-Projects'
    ];
  const images = [
    'images/cmpbio.jpeg', 
    'images/polsci.jpeg', 
    'images/pman.jpeg', 
    'images/port.jpeg', 
    'images/chess.jpeg', 
    'images/ds.png'
    ]; 
  const index = Math.floor(Math.random() * urls.length);
  const url = urls[index];
  const image = images[index];
  const greetingContainer = document.getElementById('content');
  greetingContainer.innerHTML = presentProj(url, image);
}

/**
 * Formats given url and img for display.
 */
function presentProj(url, img) {
  return `<center><img src="${img}" alt="genes" style="width:33%">
    <h2><a href="${url}">View Project Here</a></h2>`;
}
