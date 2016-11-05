import sys
import os
import copy

INPUT_FILE_NAME = "target_file9.txt"

class Area():
  def __init__(self,height,width):
    self.height = height
    self.width = width

class Window():
  def __init__(self,l,r,h,score):
    self.l = l
    self.r = r
    self.h = h # height = the sweeping line 
    self.score = score

  def __str__(self):
    txt = "win[l: " + str(self.l) + " r: " + str(self.r)
    txt += " h: " + str(self.h) + " s: " + str(self.score) + "]"
    return txt

  def __eq__(self, other):
    '''Check class equality'''
    # print "self other:", self, "|", other
    if isinstance(other, self.__class__):
      return self.__dict__ == other.__dict__
    else:
      return False
        
  def to_rec_top(self, coverage, field):
    return min(field.height, self.h + coverage.height/2)

  def to_rec_bottom(self, coverage, field):
    return max(0, self.h - coverage.height/2)

  def to_rec_right(self, coverage, field):
    return min(field.width, self.l + coverage.width/2)

  def to_rec_left(self, coverage, field):
    return max(0, self.l - coverage.width/2)  

class Rectangle():
  def __init__(self,x1,y1,x2,y2,weight): 
    #          |-----| (x2, y2)
    #          |     |    
    # (x1, y1) |-----|
    self.x1 = x1
    self.x2 = x2
    self.y1 = y1
    self.y2 = y2
    self.weight = weight # a reading value

  def __str__(self):
    txt = "rec[(x1: " +str(self.x1) + ", y1: " +str(self.y1) + ")"
    txt += ", (x2: " +str(self.x2) + ", y2: " +str(self.y2) + ")"
    txt += ", weight: " + str(self.weight) + "]\n"
    return txt
    
def win_to_rectangle(win, coverage, field):
  return Rectangle(max(0, win.l - coverage.width/2.0),
                   max(0, win.h - coverage.height/2.0),
                   min(field.width, win.l + coverage.width/2.0),
                   min(field.height, win.h + coverage.height/2.0),
                   win.score)

def percent_ov(rec1, rec_ref):
  x_ov = max(0,
             min(rec1.x2, rec_ref.x2) - max(rec1.x1, rec_ref.x1))
  y_ov = max(0,
             min(rec1.y2, rec_ref.y2) - max(rec1.y1, rec_ref.y1))
  
  rec_area = coverage.height * coverage.width
  s_intersection = x_ov * y_ov
  s_union = rec_area * 2 - s_intersection
  percent_ov = s_intersection/s_union * 100

  # print "y_ov: ", y_ov
  # print "x_ov: ", x_ov
  # print "s_intersection: ", s_intersection
  # print "s_union: ", s_union
  # print "percent_ov: ", percent_ov
  return percent_ov

def read_input_line(fin):
  l1 = fin.readline()
  l1 = l1.replace('\n','').replace('\t',' ')
  l1 = l1.split()
  # print "read_input_line:", l1
  return l1

if __name__ == "__main__":
  # coverage = Area(50, 50)
  # field = Area(500, 500)
  # win1 = Window(100, 100, 100, 40)
  # win2 = Window(100, 100, 100, 50)
  # rec1 = win_to_rectangle(win1, coverage, field)
  # rec2 = win_to_rectangle(win2, coverage, field)
  # print "win1:", win1
  # print "rec1:", rec1
  # x_ov = max(0, min(rec1.x2, rec2.x2) - max(rec1.x1, rec2.x1))
  # y_ov = max(0, min(rec1.y2, rec2.y2) - max(rec1.y1, rec2.y1))
  # rec_area = coverage.height * coverage.width
  # s_intersection = x_ov * y_ov
  # s_union = rec_area * 2 - s_intersection
  # percent_ov = s_intersection/s_union * 100
  
  # print "y_ov: ", y_ov
  # print "x_ov: ", x_ov
  # print "s_intersection: ", s_intersection
  # print "s_union: ", s_union
  # print "percent_ov: ", percent_ov
  
  print "input: ", sys.argv
  index_ref = int(sys.argv[1])
  print "index_ref: ", index_ref
  fin = open(INPUT_FILE_NAME, 'r+')
  l1 = read_input_line(fin)
  field = Area(float(l1[0]), float(l1[1]))
  l1 = read_input_line(fin)
  coverage = Area(float(l1[0]), float(l1[1]))
  line = fin.readlines()
  list_window = []
  last_index = -1
  win_list = []
  for l in line:
    l = l.replace('\n','').replace('\t',' ')
    print "line: ", l
    l = l.split()
    if last_index > int(l[1]):
      list_window.append(win_list)
      win_list = []
    last_index = int(l[1])
    win_list.append(Window(float(l[2]), float(l[3]), float(l[4]), float(l[5])))
  list_window.append(win_list)
  fin.close()
  
  for lst in list_window:
    print "[", 
    for w in lst:
      print w,
    print "]"

  print "^"*50
  print "percent error of the area with reference index: ", index_ref
  fout = open("res" + INPUT_FILE_NAME, 'w')
  fout.write(",".join("k=" + str(n) for n in range(2)))
  fout.write(",total\n")
  ref_list = list_window[index_ref]
  for k in range(len(list_window)):
    sum_err = []
    lst = list_window[k]
    print "time: ", k, 
    for i in range(len(lst)):
      rec = win_to_rectangle(lst[i], coverage, field)
      rec_ref = win_to_rectangle(ref_list[i], coverage, field)
      per_err = percent_ov(rec, rec_ref)
      sum_err.append(per_err)

    fout.write(str(k) + " " + ",".join(str(n) for n in sum_err) + ", " + str(sum(sum_err)) + "\n")
  fout.close()
