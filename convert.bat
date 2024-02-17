rem mogrify -path ancient_spell_temp -channel RGB -evaluate subtract 80%% -format png ancient_spell_source/*.png

rem mogrify -path arceuus_spell_temp -channel RGB -evaluate subtract 80%% -format png arceuus_spell_source/*.png

rem mogrify -path lunar_spell_temp -channel RGB -evaluate subtract 80%% -format png lunar_spell_source/*.png

rem mogrify -path normal_spell_temp -channel RGB -evaluate subtract 80%% -format png normal_spell_source/*.png

mogrify -path ancient_spell_temp -channel RGB -brightness-contrast -90x-70 evaluate subtract 80%% -format png ancient_spell_source/*.png

mogrify -path arceuus_spell_temp -channel RGB -brightness-contrast -90x-70 evaluate subtract 80%% -format png arceuus_spell_source/*.png

mogrify -path lunar_spell_temp -channel RGB -brightness-contrast -90x-70 evaluate subtract 80%% -format png lunar_spell_source/*.png

mogrify -path normal_spell_temp -channel RGB -brightness-contrast -90x-70 evaluate subtract 80%% -format png normal_spell_source/*.png
