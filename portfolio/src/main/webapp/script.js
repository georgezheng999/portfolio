/**
 * Fetches a message from the server and adds it to the DOM.
 */
async function getMsg() {
  const response = await fetch('/data');
  const quote = await response.text();
  document.getElementById('message-container').innerText = quote;
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
