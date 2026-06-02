/**
 * News feed (/feed)
 */
module.exports = {
  url() {
    return `${this.api.launchUrl}/feed`;
  },
  elements: {
    publishArea: {
      selector: '#publishArea',
    },
    postForm: {
      selector: '#publishArea form',
    },
    postContent: {
      selector: '#publishArea textarea[name="content"]',
    },
    postSubmit: {
      selector: '#publishArea .btn-publish',
    },
    postsList: {
      selector: '#postsContainer',
    },
    privacyLink: {
      selector: 'a[href="/privacy"]',
    },
  },
  commands: [
    {
      /**
       * Fill and submit the post form in one browser context (avoids stale refs after reload).
       * @param {string} content
       */
      publishTextPost(content) {
        return this.waitForElementVisible('@publishArea').execute(
          function (text) {
            const textarea = document.querySelector(
              '#publishArea textarea[name="content"]'
            );
            const btn = document.querySelector('#publishArea .btn-publish');
            if (!textarea || !btn) {
              throw new Error('feed publish form not found');
            }
            textarea.value = text;
            textarea.dispatchEvent(new Event('input', { bubbles: true }));
            btn.click();
          },
          [content]
        );
      },
    },
  ],
};
