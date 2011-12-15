require 'json'

require File.expand_path '../models/user', __FILE__
require File.expand_path '../models/users', __FILE__
require File.expand_path '../models/timeline', __FILE__
require File.expand_path '../models/timelines', __FILE__
require File.expand_path '../models/segment', __FILE__
require File.expand_path '../models/segments', __FILE__

require File.expand_path '../models/storage', __FILE__

require 'sinatra'

require File.expand_path '../routes/users', __FILE__
require File.expand_path '../routes/user', __FILE__
require File.expand_path '../routes/timelines', __FILE__
require File.expand_path '../routes/timeline', __FILE__
require File.expand_path '../routes/segments', __FILE__
require File.expand_path '../routes/segment', __FILE__