package musicModes;

import java.io.IOException;

/*
 *  Copyright 2020 Alexander Liebald
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

/**
 * @author Alexander Liebald
 */
public interface MusicMode {
	/**
	 * This method gets called when a beat is detected.
	 * Depending on the implementation of this method, a
	 * light effect will be executed
	 */
	void beat() throws IOException;

	/**
	 * This method gets called when no beat is detected.
	 * Depending on the implementation of this method, a
	 * light effect will be executed. Some implementations also
	 * do nothing when this method is called.
	 */
	void maintain() throws IOException;
}
