'use strict';

PDFJS.getDocument('helloworld.pdf').then(function(pdf) {
    pdf.getPage(1).then(function(page) {
        var scale = 1.5;
        var viewport = page.getViewport(scale);

        var canvas = document.getElementById('the-canvas');
        var context = canvas.getContext('2d');
        canvas.height = viewport.height;
        canvas.width = viewport.width;

        var renderContext = {
            canvasContext: context,
            viewport: viewport
        };
        page.render(renderContext);

        pdf.getData().then(function(data) {
            var blob = PDFJS.createBlob(data, 'application/pdf');
            var blobUrl = URL.createObjectURL(blob);
        
            var downloadLink = document.createElement('a');
            downloadLink.textContent = '[Download]';
            downloadLink.href = blobUrl;
            if ('download' in downloadLink) {
                downloadLink.download = 'helloworld.pdf';
            }
            (document.body || document.documentElement).appendChild(downloadLink);

            var printLink = document.createElement('a');
            printLink.textContent = '[Open in new tab]';
            printLink.href = blobUrl;
            printLink.target = '_blank';
            (document.body || document.documentElement).appendChild(printLink);
        });
    });
});
