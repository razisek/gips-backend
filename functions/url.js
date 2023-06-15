const urlPath = (type) => {
    let path = '';
    if (type === 'avatar') {
        path = '/static/avatar/';
    }

    return path;
}

const func = {
    urlPath
}

module.exports = func;