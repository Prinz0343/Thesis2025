package com.example.combinenavigation;

import android.os.Bundle;
import android.os.Handler;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.BackgroundColorSpan;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {

    private ImageButton fabToggle;
    private LinearLayout bottomNavigation;
    private ScrollView scrollView;
    private boolean isNavVisible = false;
    private float lastScrollY = 0f;
    private Handler handler = new Handler();
    private boolean isFABTouched = false;

    private TextView text1, text2;
    private EditText searchInput;
    private float currentFontSize = 16f;
    private int pageIndex = 0;

    private View header;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize views
        text1 = findViewById(R.id.text1);
        text2 = findViewById(R.id.text2);
        searchInput = findViewById(R.id.search_input);
        fabToggle = findViewById(R.id.fab_toggle);
        bottomNavigation = findViewById(R.id.bottom_navigation);
        scrollView = findViewById(R.id.scrollView);
        header = findViewById(R.id.header);

        ImageButton plusButton = findViewById(R.id.plus_button);
        ImageButton minusButton = findViewById(R.id.minus_button);
        ImageButton searchButton = findViewById(R.id.search_button);
        ImageButton nextButton = findViewById(R.id.next_button);
        ImageButton backButton = findViewById(R.id.back_button);

        fabToggle.setAlpha(0.5f);
        searchInput.setVisibility(View.GONE);

        fabToggle.setOnClickListener(v -> toggleNavigation());

        fabToggle.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                isFABTouched = true;
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                isFABTouched = false;
            }
            return false;
        });

        plusButton.setOnClickListener(v -> {
            if (currentFontSize < 30f) {
                currentFontSize += 2f;
                updateFontSizes();
            }
        });

        minusButton.setOnClickListener(v -> {
            if (currentFontSize > 10f) {
                currentFontSize -= 2f;
                updateFontSizes();
            }
        });

        searchInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                String query = searchInput.getText().toString().toLowerCase();
                searchAndHighlight(query);
                return true;
            }
            return false;
        });

        searchButton.setOnClickListener(v -> {
            if (searchInput.getVisibility() == View.GONE) {
                searchInput.setVisibility(View.VISIBLE);
                searchInput.requestFocus();
            } else {
                String query = searchInput.getText().toString().trim().toLowerCase();
                searchAndHighlight(query);
            }
        });

        nextButton.setOnClickListener(v -> {
            if (pageIndex < 2) {
                pageIndex++;
                updateContent();
            }
        });

        backButton.setOnClickListener(v -> {
            if (pageIndex > 0) {
                pageIndex--;
                updateContent();
            }
        });

        scrollView.getViewTreeObserver().addOnScrollChangedListener(() -> {
            float currentScrollY = scrollView.getScrollY();

            if (currentScrollY > lastScrollY + 10) { // Scrolling Down
                hideNavbarAndHeader();
                updateFABVisibilityOnScroll(false);
            } else if (currentScrollY < lastScrollY - 10) { // Scrolling Up
                showNavbarAndHeader();
                updateFABVisibilityOnScroll(true);
            }

            lastScrollY = currentScrollY;
        });

        updateContent();
    }

    private void toggleNavigation() {
        isNavVisible = !isNavVisible;

        RotateAnimation rotate = new RotateAnimation(
                isNavVisible ? 0 : 45,
                isNavVisible ? 45 : 0,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f
        );
        rotate.setDuration(300);
        rotate.setFillAfter(true);
        fabToggle.startAnimation(rotate);

        bottomNavigation.setVisibility(isNavVisible ? View.VISIBLE : View.GONE);
        fabToggle.setAlpha(isNavVisible ? 1f : 0.5f);
    }

    private void updateFontSizes() {
        text1.setTextSize(currentFontSize);
        text2.setTextSize(currentFontSize);
    }

    private void searchAndHighlight(String query) {
        if (query.isEmpty()) {
            searchInput.setVisibility(View.GONE);
            resetHighlight();
            Toast.makeText(this, "Please enter a word to search", Toast.LENGTH_SHORT).show();
            return;
        }

        text1.setBackgroundColor(ContextCompat.getColor(this, android.R.color.transparent));
        text2.setBackgroundColor(ContextCompat.getColor(this, android.R.color.transparent));

        SpannableString spannableText1 = new SpannableString(text1.getText().toString());
        SpannableString spannableText2 = new SpannableString(text2.getText().toString());

        boolean found = false;

        String text1Content = text1.getText().toString().toLowerCase();
        if (text1Content.contains(query)) {
            int start = text1Content.indexOf(query);
            int end = start + query.length();
            spannableText1.setSpan(new BackgroundColorSpan(ContextCompat.getColor(this, android.R.color.holo_blue_light)), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            found = true;
        }

        String text2Content = text2.getText().toString().toLowerCase();
        if (text2Content.contains(query)) {
            int start = text2Content.indexOf(query);
            int end = start + query.length();
            spannableText2.setSpan(new BackgroundColorSpan(ContextCompat.getColor(this, android.R.color.holo_blue_light)), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            found = true;
        }

        text1.setText(spannableText1);
        text2.setText(spannableText2);

        if (!found) {
            Toast.makeText(this, "No results found for \"" + query + "\"", Toast.LENGTH_SHORT).show();
        }
    }

    private void resetHighlight() {
        text1.setText(text1.getText().toString());
        text2.setText(text2.getText().toString());
    }

    private void updateContent() {
        ImageView imageView = findViewById(R.id.imageView);
        ImageButton nextButton = findViewById(R.id.next_button);
        ImageButton backButton = findViewById(R.id.back_button);

        switch (pageIndex) {
            case 0:
                imageView.setImageResource(R.drawable.comscie);
                text1.setText(getString(R.string.ComputerScience));
                text2.setText(getString(R.string.brief_history_computer_science));
                break;
            case 1:
                imageView.setImageResource(R.drawable.infotech);
                text1.setText(getString(R.string.what_is_it));
                text2.setText(getString(R.string.history_of_it));
                break;
            case 2:
                imageView.setImageResource(R.drawable.csvsit);
                text1.setText(getString(R.string.cs_diff_it));
                text2.setText(getString(R.string.cs_vs_it));
                break;
            default:
                pageIndex = 0;
                updateContent();
                return;
        }

        nextButton.setAlpha(pageIndex == 2 ? 0.5f : 1.0f);
        nextButton.setEnabled(pageIndex != 2);

        backButton.setAlpha(pageIndex == 0 ? 0.5f : 1.0f);
        backButton.setEnabled(pageIndex != 0);
    }

    private void hideNavbarAndHeader() {
        if (isNavVisible && bottomNavigation.getVisibility() == View.VISIBLE) {
            // Animate the navigation bar to fade out
            bottomNavigation.animate()
                    .alpha(0f)  // Fade out the navigation bar
                    .setDuration(300)
                    .withEndAction(() -> bottomNavigation.setVisibility(View.GONE))  // Hide it after fade-out
                    .start();
        }

        // Animate the header to hide it with translation and fade-out
        header.animate()
                .translationY(-header.getHeight())  // Move it up
                .alpha(0f)  // Fade out the header
                .setDuration(300)
                .start();
    }

    private void showNavbarAndHeader() {
        if (isNavVisible && bottomNavigation.getVisibility() != View.VISIBLE) {
            // Ensure the navigation bar is visible (fade in)
            bottomNavigation.setVisibility(View.VISIBLE);
            bottomNavigation.animate()
                    .alpha(1f)  // Fade in the navigation bar
                    .setDuration(300)
                    .start();
        }

        // Animate the header to show it with translation and fade-in
        header.animate()
                .translationY(0)  // Reset the position
                .alpha(1f)  // Fade in the header
                .setDuration(300)
                .start();
    }

    private void updateFABVisibilityOnScroll(boolean show) {
        // Synchronize the FAB and navigation bar visibility with fade animations
        if (isNavVisible) {
            if (show) {
                // Scroll up: Fade in both FAB and navigation bar
                fabToggle.animate()
                        .alpha(1f)  // Fade in the FAB
                        .setDuration(300)
                        .start();
                bottomNavigation.animate()
                        .alpha(1f)  // Fade in the navigation bar
                        .setDuration(300)
                        .start();
                bottomNavigation.setVisibility(View.VISIBLE);  // Ensure navigation bar is visible
            } else {
                // Scroll down: Fade out both FAB and navigation bar
                fabToggle.animate()
                        .alpha(0f)  // Fade out the FAB
                        .setDuration(300)
                        .start();
                bottomNavigation.animate()
                        .alpha(0f)  // Fade out the navigation bar
                        .setDuration(300)
                        .withEndAction(() -> bottomNavigation.setVisibility(View.INVISIBLE))  // Hide the navigation bar after fade-out
                        .start();
            }
        } else {
            // If FAB is not clicked: Hide FAB to the right when unclicked
            if (show) {
                fabToggle.animate()
                        .alpha(1f)  // Make FAB fully visible
                        .translationX(0f)  // Bring FAB back to visible position
                        .setDuration(300)
                        .start();
            } else {
                fabToggle.animate()
                        .alpha(0f)  // Make FAB fully invisible
                        .translationX(fabToggle.getWidth() + 100)  // Hide FAB off-screen
                        .setDuration(300)
                        .start();
            }
        }
    }
}
