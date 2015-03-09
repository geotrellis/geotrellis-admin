'use strict';

describe('Main', function () {
  var TryApp, component;

  beforeEach(function () {
    var container = document.createElement('div');
    container.id = 'content';
    document.body.appendChild(container);

    TryApp = require('../../../src/scripts/components/TryApp.jsx');
    component = TryApp();
  });

  it('should create a new instance of TryApp', function () {
    expect(component).toBeDefined();
  });
});
