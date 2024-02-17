import os
import re
import shutil

current_directory = os.getcwd()

def rename_and_move_files(darkened_folder, output_folder):
    # Ensure the destination folder exists
    os.makedirs(output_folder, exist_ok=True)

    # Iterate over files in the source folder
    for filename in os.listdir(darkened_folder):
        source_path = os.path.join(darkened_folder, filename)

        if 'resized' in filename:
            new_name = re.sub(r'_resized', "_disabled_resized", filename)

        else:
            new_name = re.sub(r'.png', "_disabled.png", filename)

        # Create the new path for the file in the destination folder
        destination_path = os.path.join(output_folder, new_name)

        # Rename and move the file
        shutil.copy(source_path, destination_path)
        print(f'Renamed and moved: {filename} -> {new_name}')

if __name__ == "__main__":
    # Set the source and destination folders and the regex pattern
    
    ancienttemp = os.path.join(current_directory, "ancient_spell_temp")
    ancientoutput = os.path.join(current_directory, "ancient_spell")
    
    arceuustemp = os.path.join(current_directory, "arceuus_spell_temp")
    arceuusoutput = os.path.join(current_directory, "arceuus_spell")
    
    lunartemp = os.path.join(current_directory, "lunar_spell_temp")
    lunaroutput = os.path.join(current_directory, "lunar_spell")
    
    normaltemp = os.path.join(current_directory, "normal_spell_temp")
    normaloutput = os.path.join(current_directory, "normal_spell")

    # Call the function to rename and move files
    rename_and_move_files(ancienttemp, ancientoutput)
    rename_and_move_files(arceuustemp, arceuusoutput)
    rename_and_move_files(lunartemp, lunaroutput)
    rename_and_move_files(normaltemp, normaloutput)